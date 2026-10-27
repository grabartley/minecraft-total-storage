package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.listing.ListingPage;
import com.grahambartley.totalstorage.listing.ListingRow;
import com.grahambartley.totalstorage.listing.ListingWindow;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;

public final class TerminalItemsPanel {
  private static final int ITEM_SIZE = 16;
  private static final int LINE_HEIGHT = 9;
  private static final int PLACEHOLDER_MARGIN = 6;
  private static final int HINT_GAP = 2;

  private static final float COUNT_ELEVATION = 200.0F;

  private static final String NO_STACK_COUNT = "";

  private static final int SAME_LAYER = 0;

  private final ListingScroll scroll =
      new ListingScroll(TerminalScreenLayout.LISTING_COLUMNS, TerminalScreenLayout.LISTING_ROWS);

  private final ListingPrefetch prefetch = new ListingPrefetch(scroll, ListingWindow.MAX_LENGTH);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(
          scroll, TerminalScreenLayout.SCROLLBAR_HEIGHT - 2, TerminalChrome.SCROLLER_HEIGHT);

  private ListingPage<ItemVariant> page = ListingPage.pending(0L);

  private Optional<ListingPlaceholder> placeholder = Optional.empty();

  public ListingScroll scroll() {
    return scroll;
  }

  public ListingPrefetch prefetch() {
    return prefetch;
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
    return TerminalScreenLayout.cellAt(windowX, windowY);
  }

  public ListingRow<ItemVariant> rowAt(int cell) {
    return page.at(scroll.firstIndex() + cell);
  }

  public void drawCells(DrawContext context, int originX, int originY) {
    if (placeholder.isPresent()) {
      TerminalSprite.SUNKEN.draw(
          context,
          originX + TerminalScreenLayout.GRID_X,
          originY + TerminalScreenLayout.GRID_Y,
          TerminalScreenLayout.GRID_WIDTH,
          TerminalScreenLayout.GRID_HEIGHT);
      return;
    }
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      VanillaSprite.SLOT.draw(
          context,
          originX + TerminalScreenLayout.cellX(cell),
          originY + TerminalScreenLayout.cellY(cell),
          TerminalScreenLayout.SLOT_SIZE,
          TerminalScreenLayout.SLOT_SIZE);
    }
  }

  public void drawScrollbar(DrawContext context, int originX, int originY) {
    TerminalChrome.drawScroller(
        context,
        scrollbar,
        originX + TerminalScreenLayout.SCROLLBAR_X,
        originY + TerminalScreenLayout.SCROLLBAR_Y,
        TerminalScreenLayout.SCROLLBAR_HEIGHT);
  }

  public boolean grabScrollbar(int windowY) {
    return scrollbar.grab(windowY - TerminalScreenLayout.SCROLLBAR_Y - 1);
  }

  public void dragScrollbarTo(int windowY) {
    scrollbar.dragTo(windowY - TerminalScreenLayout.SCROLLBAR_Y - 1);
  }

  public void drawRows(DrawContext context, TextRenderer textRenderer, int hovered) {
    placeholder.ifPresent(shown -> drawPlaceholder(context, textRenderer, shown));
    if (!page.status().hasRows()) {
      return;
    }
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      final ListingRow<ItemVariant> row = rowAt(cell);
      if (row == null) {
        continue;
      }
      final int itemX = TerminalScreenLayout.cellX(cell) + TerminalScreenLayout.SLOT_ITEM_OFFSET;
      final int itemY = TerminalScreenLayout.cellY(cell) + TerminalScreenLayout.SLOT_ITEM_OFFSET;
      final ItemStack stack = row.variant().toStack();
      context.drawItem(stack, itemX, itemY);
      drawCount(context, textRenderer, ItemCountFormat.cell(row.count()), itemX, itemY);
      context.drawItemInSlot(textRenderer, stack, itemX, itemY, NO_STACK_COUNT);
    }
    drawHighlight(context, hovered);
  }

  private static void drawHighlight(DrawContext context, int hovered) {
    if (hovered == TerminalScreenLayout.NO_CELL) {
      return;
    }
    HandledScreen.drawSlotHighlight(
        context,
        TerminalScreenLayout.cellX(hovered) + TerminalScreenLayout.SLOT_ITEM_OFFSET,
        TerminalScreenLayout.cellY(hovered) + TerminalScreenLayout.SLOT_ITEM_OFFSET,
        SAME_LAYER);
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
        VanillaPalette.opaque(VanillaPalette.COUNT),
        true);
    context.getMatrices().pop();
  }

  private static void drawPlaceholder(
      DrawContext context, TextRenderer textRenderer, ListingPlaceholder shown) {
    final int centreX = TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH / 2;
    final int room = TerminalScreenLayout.GRID_WIDTH - 2 * PLACEHOLDER_MARGIN;
    final List<OrderedText> message = textRenderer.wrapLines(shown.message(), room);
    final List<OrderedText> hint =
        shown.hint().map(text -> textRenderer.wrapLines(text, room)).orElse(List.of());
    final int block =
        ListingPlaceholder.SPRITE_SIZE
            + ListingPlaceholder.MESSAGE_GAP
            + message.size() * LINE_HEIGHT
            + (hint.isEmpty() ? 0 : HINT_GAP + hint.size() * LINE_HEIGHT);
    final int top =
        Math.max(
            TerminalScreenLayout.GRID_Y + 2,
            TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - block) / 2);
    shown
        .sprite()
        .draw(
            context,
            centreX - ListingPlaceholder.SPRITE_SIZE / 2,
            top,
            ListingPlaceholder.SPRITE_SIZE,
            ListingPlaceholder.SPRITE_SIZE);
    int lineY = top + ListingPlaceholder.SPRITE_SIZE + ListingPlaceholder.MESSAGE_GAP;
    lineY =
        drawCentred(context, textRenderer, message, centreX, lineY, VanillaPalette.BEVEL_HIGHLIGHT);
    drawCentred(context, textRenderer, hint, centreX, lineY + HINT_GAP, VanillaPalette.FIELD_TEXT);
  }

  private static int drawCentred(
      DrawContext context,
      TextRenderer textRenderer,
      List<OrderedText> lines,
      int centreX,
      int top,
      int colour) {
    int lineY = top;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          centreX - textRenderer.getWidth(line) / 2,
          lineY,
          VanillaPalette.opaque(colour),
          true);
      lineY += LINE_HEIGHT;
    }
    return lineY;
  }
}
