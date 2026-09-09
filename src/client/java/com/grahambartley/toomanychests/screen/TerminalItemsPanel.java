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

public final class TerminalItemsPanel {
  private static final Identifier CELL = TerminalSprites.of("slot/cell");
  private static final Identifier CELL_HOVER = TerminalSprites.of("slot/cell_hover");
  private static final Identifier SCROLL_TRACK = TerminalSprites.of("scroll/track");
  private static final Identifier SCROLL_THUMB = TerminalSprites.of("scroll/thumb");
  private static final Identifier SCROLL_THUMB_DISABLED =
      TerminalSprites.of("scroll/thumb_disabled");

  private static final int ITEM_SIZE = 16;
  private static final int LINE_HEIGHT = 9;

  private static final float COUNT_ELEVATION = 200.0F;

  private static final int COUNT_COLOUR = 0xFFFFFF;

  private static final int PLACEHOLDER_COLOUR = 0x9AA3B3;

  private final ListingScroll scroll =
      new ListingScroll(TerminalScreenLayout.LISTING_COLUMNS, TerminalScreenLayout.LISTING_ROWS);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(scroll, TerminalScreenLayout.SCROLLBAR_HEIGHT);

  private ListingPage<ItemVariant> page = ListingPage.pending(0L);

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

  public void beginFrame(ListingPage<ItemVariant> shown) {
    page = shown;
    placeholder = ListingPlaceholder.forStatus(page.status());
    scroll.fit(page.totalMatching());
  }

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

  public ListingRow<ItemVariant> rowAt(int cell) {
    return page.at(scroll.firstIndex() + cell);
  }

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
