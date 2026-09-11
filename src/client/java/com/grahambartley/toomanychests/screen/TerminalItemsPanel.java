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

  private static final int HIGHLIGHT_ELEVATION = 0;

  private static final String NO_STACK_COUNT = "";

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
    TerminalSprite.SUNKEN.draw(
        context,
        originX + TerminalScreenLayout.SCROLLBAR_X,
        originY + TerminalScreenLayout.SCROLLBAR_Y,
        TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalScreenLayout.SCROLLBAR_HEIGHT);
    (scrollbar.isEnabled() ? TerminalSprite.RAISED : TerminalSprite.FLAT)
        .draw(
            context,
            originX + TerminalScreenLayout.SCROLLBAR_X,
            originY + TerminalScreenLayout.SCROLLBAR_Y + scrollbar.thumbTop(),
            TerminalScreenLayout.SCROLLBAR_WIDTH,
            scrollbar.thumbHeight());
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
      context.drawItemInSlot(textRenderer, stack, itemX, itemY, NO_STACK_COUNT);
      drawCount(context, textRenderer, ItemCountFormat.cell(row.count()), itemX, itemY);
      if (cell == hovered) {
        HandledScreen.drawSlotHighlight(context, itemX, itemY, HIGHLIGHT_ELEVATION);
      }
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
