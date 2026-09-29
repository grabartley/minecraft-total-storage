package com.grahambartley.toomanychests.screen;

import net.minecraft.client.gui.DrawContext;

public final class TagPromptList {
  private final ListingScroll scroll = new ListingScroll(1, TagPromptLayout.VISIBLE_ROWS);

  private final TerminalScrollbar scrollbar =
      new TerminalScrollbar(scroll, TagPromptLayout.TRACK_HEIGHT);

  public void reset() {
    scrollbar.release();
    scroll.scrollTo(0);
  }

  public void fit(int entries) {
    scroll.fit(entries);
  }

  public boolean scrollBy(int rows) {
    return scroll.scrollBy(rows);
  }

  public boolean overflows() {
    return scroll.overflows();
  }

  public int indexOfRow(int row) {
    return scroll.firstIndex() + row;
  }

  public int indexAt(int windowX, int windowY) {
    final int row = TagPromptLayout.rowAt(windowX, windowY);
    return row == TagPromptLayout.NO_ROW ? TagPromptLayout.NO_ROW : indexOfRow(row);
  }

  public boolean grabScrollbar(int windowX, int windowY) {
    return TagPromptLayout.isOverScrollbar(windowX, windowY)
        && scrollbar.grab(windowY - TagPromptLayout.rowsTop());
  }

  public boolean isDraggingScrollbar() {
    return scrollbar.isDragging();
  }

  public void dragScrollbarTo(int windowY) {
    scrollbar.dragTo(windowY - TagPromptLayout.rowsTop());
  }

  public void releaseScrollbar() {
    scrollbar.release();
  }

  public int thumbTop() {
    return scrollbar.thumbTop();
  }

  public void drawScrollbar(DrawContext context, int originX, int originY) {
    TerminalChrome.drawScrollbar(
        context,
        scrollbar,
        originX + TagPromptLayout.scrollbarX(),
        originY + TagPromptLayout.rowsTop(),
        TagPromptLayout.SCROLLBAR_WIDTH,
        TagPromptLayout.TRACK_HEIGHT);
  }
}
