package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.FilterColumnLayout;
import com.grahambartley.toomanychests.terminal.NetworkTagEntry;
import com.grahambartley.toomanychests.terminal.NetworkTagView;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class TerminalFilterColumn {
  private static final Identifier ROW = TerminalSprites.of("filter/row");
  private static final Identifier ROW_HOVER = TerminalSprites.of("filter/row_hover");
  private static final Identifier ROW_ACTIVE = TerminalSprites.of("filter/row_active");
  private static final Identifier CONTROL = TerminalSprites.of("button/normal");
  private static final Identifier CONTROL_HOVER = TerminalSprites.of("button/hover");
  private static final Identifier CONTROL_DISABLED = TerminalSprites.of("button/disabled");
  private static final Identifier CLEAR_ICON = TerminalSprites.of("icon/clear_all");
  private static final Identifier SCROLL_TRACK = TerminalSprites.of("scroll/track");
  private static final Identifier SCROLL_THUMB = TerminalSprites.of("scroll/thumb");
  private static final Identifier SCROLL_THUMB_DISABLED =
      TerminalSprites.of("scroll/thumb_disabled");

  private static final int LABEL_COLOUR = 0xE4E9F2;
  private static final int MUTED_COLOUR = 0x9AA3B3;
  private static final int EMPTY_COLOUR = 0x6D7484;
  private static final int LINE_HEIGHT = 9;
  private static final int ICON_SIZE = 8;

  private final ListingScroll scroll = new ListingScroll(1, FilterColumnLayout.ROWS);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(scroll, FilterColumnLayout.CONTENT_HEIGHT);

  public ListingScroll scroll() {
    return scroll;
  }

  public void fit(NetworkTagView view) {
    scroll.fit(view.tags().size());
  }

  public NetworkTagEntry tagAt(NetworkTagView view, int windowX, int windowY) {
    final int index = rowAt(windowX, windowY);
    final List<NetworkTagEntry> tags = view.tags();
    return index < 0 || index >= tags.size() ? null : tags.get(index);
  }

  public int rowAt(int windowX, int windowY) {
    if (FilterColumnLayout.isOverScrollbar(windowX, windowY)) {
      return FilterColumnLayout.NO_ROW;
    }
    final int row = FilterColumnLayout.rowAt(windowX, windowY);
    return row == FilterColumnLayout.NO_ROW ? row : scroll.firstIndex() + row;
  }

  public boolean grabScrollbar(int windowY) {
    return scrollbar.grab(windowY - FilterColumnLayout.CONTENT_Y);
  }

  public boolean isDraggingScrollbar() {
    return scrollbar.isDragging();
  }

  public void dragScrollbarTo(int windowY) {
    scrollbar.dragTo(windowY - FilterColumnLayout.CONTENT_Y);
  }

  public void releaseScrollbar() {
    scrollbar.release();
  }

  public boolean click(
      NetworkTagView view, FilterSelection selection, int windowX, int windowY, int button) {
    if (button != GridClick.LEFT_BUTTON || !FilterColumnLayout.isOverColumn(windowX, windowY)) {
      return false;
    }
    if (FilterColumnLayout.isOverClear(windowX, windowY)) {
      selection.clear();
      return true;
    }
    if (FilterColumnLayout.isOverMode(windowX, windowY)) {
      selection.cycleMode();
      return true;
    }
    if (FilterColumnLayout.isOverScrollbar(windowX, windowY)) {
      grabScrollbar(windowY);
      return true;
    }
    final NetworkTagEntry tag = tagAt(view, windowX, windowY);
    if (tag != null) {
      selection.toggle(tag.id());
    }
    return true;
  }

  public void draw(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      FilterSelection selection,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    drawHeader(context, textRenderer, selection, originX, originY, mouseX, mouseY);
    drawRows(context, textRenderer, view, selection, originX, originY, mouseX, mouseY);
    drawScrollbar(context, originX, originY);
  }

  private void drawHeader(
      DrawContext context,
      TextRenderer textRenderer,
      FilterSelection selection,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final int top = originY + FilterColumnLayout.Y;
    context.drawGuiTexture(
        FilterColumnLayout.isOverMode(mouseX, mouseY) ? CONTROL_HOVER : CONTROL,
        originX + FilterColumnLayout.X,
        top,
        FilterColumnLayout.MODE_WIDTH,
        FilterColumnLayout.HEADER_HEIGHT);
    final Text mode = FilterColumnText.mode(selection.mode());
    context.drawText(
        textRenderer,
        mode,
        originX
            + FilterColumnLayout.X
            + (FilterColumnLayout.MODE_WIDTH - textRenderer.getWidth(mode)) / 2,
        top + (FilterColumnLayout.HEADER_HEIGHT - LINE_HEIGHT) / 2 + 1,
        selection.isFiltering() ? LABEL_COLOUR : MUTED_COLOUR,
        false);
    final boolean clearable = selection.isFiltering();
    context.drawGuiTexture(
        !clearable
            ? CONTROL_DISABLED
            : FilterColumnLayout.isOverClear(mouseX, mouseY) ? CONTROL_HOVER : CONTROL,
        originX + FilterColumnLayout.CLEAR_X,
        top,
        FilterColumnLayout.CONTROL_SIZE,
        FilterColumnLayout.CONTROL_SIZE);
    final int iconInset = (FilterColumnLayout.CONTROL_SIZE - ICON_SIZE) / 2;
    context.drawGuiTexture(
        CLEAR_ICON,
        originX + FilterColumnLayout.CLEAR_X + iconInset,
        top + iconInset,
        ICON_SIZE,
        ICON_SIZE);
  }

  private void drawRows(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagView view,
      FilterSelection selection,
      int originX,
      int originY,
      int mouseX,
      int mouseY) {
    final List<NetworkTagEntry> tags = view.tags();
    final int hovered = rowAt(mouseX, mouseY);
    for (int row = 0; row < FilterColumnLayout.ROWS; row++) {
      final int index = scroll.firstIndex() + row;
      if (index >= tags.size()) {
        return;
      }
      drawRow(
          context,
          textRenderer,
          tags.get(index),
          selection.isActive(tags.get(index).id()),
          index == hovered,
          originX,
          originY + FilterColumnLayout.rowY(row));
    }
  }

  private static void drawRow(
      DrawContext context,
      TextRenderer textRenderer,
      NetworkTagEntry tag,
      boolean active,
      boolean hovered,
      int originX,
      int top) {
    final int left = originX + FilterColumnLayout.X;
    context.drawGuiTexture(
        active ? ROW_ACTIVE : hovered ? ROW_HOVER : ROW,
        left,
        top,
        FilterColumnLayout.ROW_WIDTH,
        FilterColumnLayout.ROW_HEIGHT);
    final String count = FilterColumnText.count(tag);
    final int countWidth = textRenderer.getWidth(count);
    final int textY = top + (FilterColumnLayout.ROW_HEIGHT - LINE_HEIGHT) / 2 + 1;
    final int nameWidth =
        FilterColumnLayout.ROW_WIDTH
            - 2 * FilterColumnLayout.TEXT_MARGIN
            - countWidth
            - FilterColumnLayout.TEXT_MARGIN;
    final OrderedText name =
        TrimmedText.toWidth(textRenderer, FilterColumnText.columnNameOf(tag), nameWidth);
    context.drawText(
        textRenderer,
        name,
        left + FilterColumnLayout.TEXT_MARGIN,
        textY,
        active ? LABEL_COLOUR : tag.isEmptyHere() ? EMPTY_COLOUR : MUTED_COLOUR,
        false);
    context.drawText(
        textRenderer,
        count,
        left + FilterColumnLayout.ROW_WIDTH - FilterColumnLayout.TEXT_MARGIN - countWidth,
        textY,
        tag.isEmptyHere() ? EMPTY_COLOUR : MUTED_COLOUR,
        false);
  }

  private void drawScrollbar(DrawContext context, int originX, int originY) {
    final int left = originX + FilterColumnLayout.SCROLLBAR_X;
    final int top = originY + FilterColumnLayout.CONTENT_Y;
    context.drawGuiTexture(
        SCROLL_TRACK,
        left,
        top,
        FilterColumnLayout.SCROLLBAR_WIDTH,
        FilterColumnLayout.CONTENT_HEIGHT);
    context.drawGuiTexture(
        scrollbar.isEnabled() ? SCROLL_THUMB : SCROLL_THUMB_DISABLED,
        left,
        top + scrollbar.thumbTop(),
        FilterColumnLayout.SCROLLBAR_WIDTH,
        scrollbar.thumbHeight());
  }
}
