package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

/**
 * Draws what the network holds, and says which cell a click landed on.
 *
 * <p>Split from the screen so the two views the terminal offers are the same shape: the screen owns
 * the chassis, the tabs and the clicks, and each panel owns one recess full of content. Without the
 * split the screen would be the items view with a settings view bolted to the side of it.
 *
 * <p>The grid is not made of slots and cannot be. Per ADR 0025 the network is not an inventory, so
 * a cell is drawn from a row of the page the server last sent rather than from anything the handler
 * addresses, and a cell holds a variant and a total rather than a stack.
 *
 * <p>Cells are addressed by their position in the whole listing rather than by their position in
 * the page in hand. A scroll and the page answering it are a round trip apart, and drawing by page
 * position would slide every cell sideways for that round trip instead of leaving the rows that
 * still overlap exactly where they were.
 */
public final class TerminalItemsPanel {
  private static final Identifier CELL = TerminalSprites.of("slot/cell");
  private static final Identifier CELL_HOVER = TerminalSprites.of("slot/cell_hover");
  private static final Identifier SCROLL_TRACK = TerminalSprites.of("scroll/track");
  private static final Identifier SCROLL_THUMB = TerminalSprites.of("scroll/thumb");
  private static final Identifier SCROLL_THUMB_DISABLED =
      TerminalSprites.of("scroll/thumb_disabled");

  private static final int ITEM_SIZE = 16;
  private static final int LINE_HEIGHT = 9;

  /** Far enough forward to clear the item model the count is written across. */
  private static final float COUNT_ELEVATION = 200.0F;

  private static final int COUNT_COLOUR = 0xFFFFFF;

  /** The palette's mid glyph, which is what reads on the recess floor behind an empty grid. */
  private static final int PLACEHOLDER_COLOUR = 0x9AA3B3;

  private final ListingScroll scroll =
      new ListingScroll(TerminalScreenLayout.LISTING_COLUMNS, TerminalScreenLayout.LISTING_ROWS);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(scroll, TerminalScreenLayout.SCROLLBAR_HEIGHT);

  /**
   * The page every part of one frame draws from, taken once at the top of it. Reading the mirror
   * again partway through would let the chassis, the cells and the tooltip disagree about what is
   * on screen.
   */
  private ListingPage<ItemVariant> page = ListingPage.pending(0L);

  /** What the grid is showing instead of rows, taken with the page so the two cannot disagree. */
  private Optional<ListingPlaceholder> placeholder = Optional.empty();

  public ListingScroll scroll() {
    return scroll;
  }

  public TerminalScrollbar scrollbar() {
    return scrollbar;
  }

  public ListingPage<ItemVariant> page() {
    return page;
  }

  /** Takes the page this frame draws from, so nothing later in it reads the mirror again. */
  public void beginFrame(ListingPage<ItemVariant> shown) {
    page = shown;
    placeholder = ListingPlaceholder.forStatus(page.status());
    scroll.fit(page.totalMatching());
  }

  /** The cell under the pointer, or {@link TerminalScreenLayout#NO_CELL} when it holds no row. */
  public int hoveredCell(int windowX, int windowY) {
    if (!page.status().hasRows()) {
      return TerminalScreenLayout.NO_CELL;
    }
    final int cell = TerminalScreenLayout.cellAt(windowX, windowY);
    if (cell == TerminalScreenLayout.NO_CELL || rowAt(cell) == null) {
      return TerminalScreenLayout.NO_CELL;
    }
    return cell;
  }

  /**
   * The row drawn in a cell, addressed by its place in the whole listing.
   *
   * <p>Not unit tested, unlike {@link TerminalSettingsPanel#rowAt}, because a page of rows needs an
   * {@code ItemVariant} and that needs a registry. Both halves of the arithmetic are covered on
   * their own by {@code ListingPageTest} and {@code ListingScrollTest}, and the composition is
   * exercised end to end by the terminal's automated QA run.
   */
  public ListingRow<ItemVariant> rowAt(int cell) {
    return page.at(scroll.firstIndex() + cell);
  }

  /**
   * A grid explaining why it has no rows draws no cells. The empty-state art was drawn against the
   * recess floor, and forty-five cells behind it leave the illustration showing through the gutters
   * between them rather than reading as a picture.
   */
  public void drawCells(DrawContext context, int originX, int originY, int hovered) {
    if (placeholder.isPresent()) {
      return;
    }
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      context.drawGuiTexture(
          cell == hovered ? CELL_HOVER : CELL,
          originX + TerminalScreenLayout.cellX(cell),
          originY + TerminalScreenLayout.cellY(cell),
          TerminalScreenLayout.CELL_SIZE,
          TerminalScreenLayout.CELL_SIZE);
    }
  }

  public void drawScrollbar(DrawContext context, int originX, int originY) {
    context.drawGuiTexture(
        SCROLL_TRACK,
        originX + TerminalScreenLayout.SCROLLBAR_X,
        originY + TerminalScreenLayout.SCROLLBAR_Y,
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalScreenLayout.SCROLLBAR_HEIGHT);
    context.drawGuiTexture(
        scrollbar.isEnabled() ? SCROLL_THUMB : SCROLL_THUMB_DISABLED,
        originX + TerminalScreenLayout.SCROLLBAR_X,
        originY + TerminalScreenLayout.SCROLLBAR_Y + scrollbar.thumbTop(),
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        scrollbar.thumbHeight());
  }

  /** Drawn in the window's own coordinates, which is what the foreground pass is translated to. */
  public void drawRows(DrawContext context, TextRenderer textRenderer) {
    placeholder.ifPresent(shown -> drawPlaceholder(context, textRenderer, shown));
    if (!page.status().hasRows()) {
      return;
    }
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      final ListingRow<ItemVariant> row = rowAt(cell);
      if (row == null) {
        continue;
      }
      final int itemX = TerminalScreenLayout.cellX(cell) + TerminalScreenLayout.CELL_ITEM_OFFSET;
      final int itemY = TerminalScreenLayout.cellY(cell) + TerminalScreenLayout.CELL_ITEM_OFFSET;
      context.drawItem(row.variant().toStack(), itemX, itemY);
      drawCount(context, textRenderer, ItemCountFormat.cell(row.count()), itemX, itemY);
    }
  }

  /**
   * The count sits in the item's bottom right corner as a stack size does, shrunk only as far as
   * the cell demands. A network total can be several times wider than the two digits vanilla ever
   * has to fit there, but most cells hold a count that fits at full size, and shrinking those too
   * turns the common case to mush at the smallest GUI scale for the sake of the rare one.
   */
  private static void drawCount(
      DrawContext context, TextRenderer textRenderer, String label, int itemX, int itemY) {
    final int width = textRenderer.getWidth(label);
    final float scale = Math.min(1.0F, (float) ITEM_SIZE / width);
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, COUNT_ELEVATION);
    context.getMatrices().scale(scale, scale, 1.0F);
    context.drawText(
        textRenderer,
        label,
        Math.round((itemX + ITEM_SIZE) / scale) - width,
        Math.round((itemY + ITEM_SIZE - LINE_HEIGHT * scale) / scale),
        COUNT_COLOUR,
        true);
    context.getMatrices().pop();
  }

  /**
   * The message is wrapped to the grid rather than trusted to fit it. English happens to be short
   * enough, but a translation is not, and one long line runs off both sides of the chassis and off
   * the screen with it.
   */
  private static void drawPlaceholder(
      DrawContext context, TextRenderer textRenderer, ListingPlaceholder shown) {
    final int centreX = TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH / 2;
    final List<OrderedText> lines =
        textRenderer.wrapLines(shown.message(), TerminalScreenLayout.GRID_WIDTH);
    final int block =
        ListingPlaceholder.SPRITE_SIZE
            + ListingPlaceholder.MESSAGE_GAP
            + lines.size() * LINE_HEIGHT;
    final int top =
        Math.max(
            TerminalScreenLayout.GRID_Y,
            TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - block) / 2);
    context.drawGuiTexture(
        shown.sprite(),
        centreX - ListingPlaceholder.SPRITE_SIZE / 2,
        top,
        ListingPlaceholder.SPRITE_SIZE,
        ListingPlaceholder.SPRITE_SIZE);
    int lineY = top + ListingPlaceholder.SPRITE_SIZE + ListingPlaceholder.MESSAGE_GAP;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          centreX - textRenderer.getWidth(line) / 2,
          lineY,
          PLACEHOLDER_COLOUR,
          false);
      lineY += LINE_HEIGHT;
    }
  }
}
