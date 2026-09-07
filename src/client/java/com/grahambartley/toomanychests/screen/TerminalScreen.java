package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.listing.TerminalListingMirror;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * The terminal's window: its chassis, the grid of everything the network holds, and the player's
 * own inventory.
 *
 * <p>The grid is not made of slots and cannot be. Per ADR 0025 the network is not an inventory, so
 * a cell is drawn from a row of the page the server last sent rather than from anything the handler
 * addresses, and a cell holds a variant and a total rather than a stack.
 *
 * <p>Cells are addressed by their position in the whole listing rather than by their position in
 * the page in hand. A scroll and the page answering it are a round trip apart, and drawing by page
 * position would slide every cell sideways for that round trip instead of leaving the rows that
 * still overlap exactly where they were.
 *
 * <p>Every piece of chassis is a nine-sliced sprite rather than one background image, per ADR 0017,
 * so the window can change size as the filter column grows without a new texture.
 */
public class TerminalScreen extends HandledScreen<TerminalScreenHandler> {
  private static final Identifier PANEL = sprite("panel/background");
  private static final Identifier INSET = sprite("panel/inset");
  private static final Identifier CELL = sprite("slot/cell");
  private static final Identifier CELL_HOVER = sprite("slot/cell_hover");
  private static final Identifier SCROLL_TRACK = sprite("scroll/track");
  private static final Identifier SCROLL_THUMB = sprite("scroll/thumb");
  private static final Identifier SCROLL_THUMB_DISABLED = sprite("scroll/thumb_disabled");

  private static final int ITEM_SIZE = 16;
  private static final int LINE_HEIGHT = 9;

  /** Counts are drawn at half size so five characters still clear the side of a cell. */
  private static final float COUNT_SCALE = 0.5F;

  private static final int COUNT_HEIGHT = Math.round(LINE_HEIGHT * COUNT_SCALE);

  /** Far enough forward to clear the item model the count is written across. */
  private static final float COUNT_ELEVATION = 200.0F;

  private static final int COUNT_COLOUR = 0xFFFFFF;
  private static final int PLACEHOLDER_COLOUR = 0xACACB8;

  private final ListingScroll scroll =
      new ListingScroll(TerminalScreenLayout.LISTING_COLUMNS, TerminalScreenLayout.LISTING_ROWS);

  private ListingWindow requested = ListingWindow.EMPTY;
  private boolean draggingThumb;
  private int thumbGrabOffset;

  public TerminalScreen(TerminalScreenHandler handler, PlayerInventory inventory, Text title) {
    super(handler, inventory, title);
    this.backgroundWidth = TerminalScreenLayout.WINDOW_WIDTH;
    this.backgroundHeight = TerminalScreenLayout.WINDOW_HEIGHT;
    this.titleX = TerminalScreenLayout.TITLE_X;
    this.titleY = TerminalScreenLayout.TITLE_Y;
    this.playerInventoryTitleX = TerminalScreenLayout.INVENTORY_LABEL_X;
    this.playerInventoryTitleY = TerminalScreenLayout.INVENTORY_LABEL_Y;
  }

  /**
   * Opening the screen asks for the first page. Nothing arrives unprompted: the server answers the
   * window a screen has told it about, so a screen that never asks is never sent anything.
   *
   * <p>Laying the window out again is not opening it. Minecraft runs this on every resize and every
   * change of GUI scale, and treating those as a fresh screen would throw away the page being shown
   * and ask for the first one again, wherever the player had scrolled to.
   */
  @Override
  protected void init() {
    super.init();
    if (TerminalListingMirror.isShowing(handler.syncId)) {
      return;
    }
    TerminalListingMirror.opened(handler.syncId);
    requestVisibleRows();
  }

  @Override
  public void removed() {
    super.removed();
    TerminalListingMirror.closed();
  }

  /**
   * Scrolling asks the server for the rows it moved onto, once a frame rather than once a notch. A
   * wheel spun hard is several notches in one frame and one window at the end of them, so the
   * requests a player generates are bounded by their frame rate rather than by their wrist.
   */
  @Override
  public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    final ListingPage<ItemVariant> page = TerminalListingMirror.page();
    scroll.fit(page.totalMatching());
    if (!scroll.window().equals(requested)) {
      requestVisibleRows();
    }
    renderBackground(context, mouseX, mouseY, delta);
    super.render(context, mouseX, mouseY, delta);
    drawMouseoverTooltip(context, mouseX, mouseY);
    drawCellTooltip(context, page, mouseX, mouseY);
  }

  @Override
  protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
    context.drawGuiTexture(PANEL, x, y, backgroundWidth, backgroundHeight);
    context.drawGuiTexture(
        INSET,
        x + TerminalScreenLayout.INSET_X,
        y + TerminalScreenLayout.INSET_Y,
        TerminalScreenLayout.INSET_WIDTH,
        TerminalScreenLayout.INSET_HEIGHT);
    drawCells(context, TerminalListingMirror.page(), mouseX, mouseY);
    drawScrollbar(context);
    for (final Slot slot : handler.slots) {
      context.drawGuiTexture(
          INSET,
          x + slot.x - 1,
          y + slot.y - 1,
          TerminalScreenLayout.SLOT_SIZE,
          TerminalScreenLayout.SLOT_SIZE);
    }
  }

  @Override
  protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
    super.drawForeground(context, mouseX, mouseY);
    final ListingPage<ItemVariant> page = TerminalListingMirror.page();
    ListingPlaceholder.forStatus(page.status())
        .ifPresent(placeholder -> drawPlaceholder(context, placeholder));
    drawRows(context, page);
  }

  @Override
  public boolean mouseScrolled(
      double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    if (verticalAmount != 0.0 && scroll.overflows() && isOverListing(mouseX, mouseY)) {
      scroll.scrollBy(verticalAmount > 0.0 ? -1 : 1);
      return true;
    }
    return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0
        && scroll.overflows()
        && TerminalScreenLayout.isOverScrollbar(windowX(mouseX), windowY(mouseY))) {
      grabThumb(windowY(mouseY));
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double deltaX, double deltaY) {
    if (draggingThumb) {
      dragThumbTo(windowY(mouseY));
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (draggingThumb) {
      draggingThumb = false;
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  private void requestVisibleRows() {
    requested = scroll.window();
    ModNetworkingClient.sendListingRequest(
        handler.syncId, requested, "", TagCombinationMode.DEFAULT, Set.of());
  }

  private void drawCells(
      DrawContext context, ListingPage<ItemVariant> page, int mouseX, int mouseY) {
    final int hovered = hoveredCell(page, mouseX, mouseY);
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      context.drawGuiTexture(
          cell == hovered ? CELL_HOVER : CELL,
          x + TerminalScreenLayout.cellX(cell),
          y + TerminalScreenLayout.cellY(cell),
          TerminalScreenLayout.CELL_SIZE,
          TerminalScreenLayout.CELL_SIZE);
    }
  }

  private void drawScrollbar(DrawContext context) {
    context.drawGuiTexture(
        SCROLL_TRACK,
        x + TerminalScreenLayout.SCROLLBAR_X,
        y + TerminalScreenLayout.SCROLLBAR_Y,
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalScreenLayout.SCROLLBAR_HEIGHT);
    final int height = thumbHeight();
    context.drawGuiTexture(
        scroll.overflows() ? SCROLL_THUMB : SCROLL_THUMB_DISABLED,
        x + TerminalScreenLayout.SCROLLBAR_X,
        y + TerminalScreenLayout.SCROLLBAR_Y + thumbTop(height),
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        height);
  }

  private void drawRows(DrawContext context, ListingPage<ItemVariant> page) {
    if (!page.status().hasRows()) {
      return;
    }
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      final ListingRow<ItemVariant> row = rowAt(page, cell);
      if (row == null) {
        continue;
      }
      final int itemX = TerminalScreenLayout.cellX(cell) + TerminalScreenLayout.CELL_ITEM_OFFSET;
      final int itemY = TerminalScreenLayout.cellY(cell) + TerminalScreenLayout.CELL_ITEM_OFFSET;
      context.drawItem(row.variant().toStack(), itemX, itemY);
      drawCount(context, ItemCountFormat.cell(row.count()), itemX, itemY);
    }
  }

  /**
   * The count sits in the item's bottom right corner as a stack size does, but scaled down, because
   * a network total is several times wider than the two digits vanilla ever has to fit there.
   */
  private void drawCount(DrawContext context, String label, int itemX, int itemY) {
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, COUNT_ELEVATION);
    context.getMatrices().scale(COUNT_SCALE, COUNT_SCALE, 1.0F);
    context.drawText(
        textRenderer,
        label,
        Math.round((itemX + ITEM_SIZE) / COUNT_SCALE) - textRenderer.getWidth(label),
        Math.round((itemY + ITEM_SIZE - COUNT_HEIGHT) / COUNT_SCALE),
        COUNT_COLOUR,
        true);
    context.getMatrices().pop();
  }

  private void drawPlaceholder(DrawContext context, ListingPlaceholder placeholder) {
    final int centreX = TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH / 2;
    final int block = ListingPlaceholder.SPRITE_SIZE + ListingPlaceholder.MESSAGE_GAP + LINE_HEIGHT;
    final int top = TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - block) / 2;
    context.drawGuiTexture(
        placeholder.sprite(),
        centreX - ListingPlaceholder.SPRITE_SIZE / 2,
        top,
        ListingPlaceholder.SPRITE_SIZE,
        ListingPlaceholder.SPRITE_SIZE);
    final Text message = placeholder.message();
    context.drawText(
        textRenderer,
        message,
        centreX - textRenderer.getWidth(message) / 2,
        top + ListingPlaceholder.SPRITE_SIZE + ListingPlaceholder.MESSAGE_GAP,
        PLACEHOLDER_COLOUR,
        false);
  }

  private void drawCellTooltip(
      DrawContext context, ListingPage<ItemVariant> page, int mouseX, int mouseY) {
    final int cell = hoveredCell(page, mouseX, mouseY);
    if (cell == TerminalScreenLayout.NO_CELL) {
      return;
    }
    final ListingRow<ItemVariant> row = rowAt(page, cell);
    final ItemStack stack = row.variant().toStack();
    final List<Text> lines = new ArrayList<>(Screen.getTooltipFromItem(client, stack));
    lines.addAll(ListingTooltip.extraLines(row));
    context.drawTooltip(textRenderer, lines, mouseX, mouseY);
  }

  /** The cell under the pointer, or {@link TerminalScreenLayout#NO_CELL} when it holds no row. */
  private int hoveredCell(ListingPage<ItemVariant> page, int mouseX, int mouseY) {
    if (!page.status().hasRows()) {
      return TerminalScreenLayout.NO_CELL;
    }
    final int cell = TerminalScreenLayout.cellAt(mouseX - x, mouseY - y);
    if (cell == TerminalScreenLayout.NO_CELL || rowAt(page, cell) == null) {
      return TerminalScreenLayout.NO_CELL;
    }
    return cell;
  }

  private ListingRow<ItemVariant> rowAt(ListingPage<ItemVariant> page, int cell) {
    return page.at(scroll.firstIndex() + cell);
  }

  private boolean isOverListing(double mouseX, double mouseY) {
    final int windowX = windowX(mouseX);
    final int windowY = windowY(mouseY);
    return TerminalScreenLayout.cellAt(windowX, windowY) != TerminalScreenLayout.NO_CELL
        || TerminalScreenLayout.isOverScrollbar(windowX, windowY);
  }

  /**
   * Grabs the thumb where it was clicked, or jumps it under the pointer when the track was clicked
   * beside it. Grabbing by the offset is what stops the thumb snapping its own centre to the
   * pointer the instant a player touches it.
   */
  private void grabThumb(int windowY) {
    final int height = thumbHeight();
    final int top = TerminalScreenLayout.SCROLLBAR_Y + thumbTop(height);
    draggingThumb = true;
    if (windowY >= top && windowY < top + height) {
      thumbGrabOffset = windowY - top;
      return;
    }
    thumbGrabOffset = height / 2;
    dragThumbTo(windowY);
  }

  private void dragThumbTo(int windowY) {
    final int height = thumbHeight();
    scroll.scrollTo(
        ScrollbarMetrics.rowAt(
            TerminalScreenLayout.SCROLLBAR_HEIGHT,
            height,
            windowY - thumbGrabOffset - TerminalScreenLayout.SCROLLBAR_Y,
            scroll.maxTopRow()));
  }

  private int thumbHeight() {
    return ScrollbarMetrics.thumbHeight(
        TerminalScreenLayout.SCROLLBAR_HEIGHT, scroll.visibleRows(), scroll.totalRows());
  }

  private int thumbTop(int height) {
    return ScrollbarMetrics.thumbTop(
        TerminalScreenLayout.SCROLLBAR_HEIGHT, height, scroll.topRow(), scroll.maxTopRow());
  }

  private int windowX(double mouseX) {
    return (int) Math.floor(mouseX) - x;
  }

  private int windowY(double mouseY) {
    return (int) Math.floor(mouseY) - y;
  }

  private static Identifier sprite(String path) {
    return Identifier.of(TooManyChests.MOD_ID, path);
  }
}
