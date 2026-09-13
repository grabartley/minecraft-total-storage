package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.ListingRow;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
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

  private static final float COUNT_ELEVATION = 200.0F;

  private static final String NO_STACK_COUNT = "";

  private static final int SAME_LAYER = 0;

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
    return TerminalScreenLayout.cellAt(windowX, windowY);
  }

  public ListingRow<ItemVariant> rowAt(int cell) {
    return page.at(scroll.firstIndex() + cell);
  }

  public void drawCells(DrawContext context, int originX, int originY) {
    if (placeholder.isPresent()) {
      return;
    }
    for (int cell = 0; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
      TerminalSprite.SUNKEN.draw(
          context,
          originX + TerminalScreenLayout.cellX(cell),
          originY + TerminalScreenLayout.cellY(cell),
          TerminalScreenLayout.SLOT_SIZE,
          TerminalScreenLayout.SLOT_SIZE);
    }
  }

  public void drawScrollbar(DrawContext context, int originX, int originY) {
    TerminalChrome.drawScrollbar(
        context,
        scrollbar,
        originX + TerminalScreenLayout.SCROLLBAR_X,
        originY + TerminalScreenLayout.SCROLLBAR_Y,
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalScreenLayout.SCROLLBAR_HEIGHT);
  }

  public void drawRows(DrawContext context, TextRenderer textRenderer, int hovered) {
    drawRows(context, textRenderer, hovered, 0);
  }

  public void drawRows(
      DrawContext context, TextRenderer textRenderer, int hovered, int hiddenRows) {
    placeholder.ifPresent(shown -> drawPlaceholder(context, textRenderer, shown));
    if (!page.status().hasRows()) {
      return;
    }
    final int firstDrawn = Math.max(0, hiddenRows) * TerminalScreenLayout.LISTING_COLUMNS;
    for (int cell = firstDrawn; cell < TerminalScreenLayout.LISTING_PAGE_SIZE; cell++) {
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
        VanillaPalette.COUNT,
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
    shown
        .sprite()
        .draw(
            context,
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
          VanillaPalette.LABEL,
          false);
      lineY += LINE_HEIGHT;
    }
  }
}
