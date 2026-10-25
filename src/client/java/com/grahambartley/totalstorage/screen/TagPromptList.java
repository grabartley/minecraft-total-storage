package com.grahambartley.totalstorage.screen;

import java.util.Objects;
import net.minecraft.client.gui.DrawContext;

public final class TagPromptList {
  private final PromptListGeometry geometry;

  private final ListingScroll scroll;

  private final TerminalScrollbar scrollbar;

  public TagPromptList() {
    this(TagPromptLayout.LIST);
  }

  public TagPromptList(PromptListGeometry geometry) {
    this.geometry = Objects.requireNonNull(geometry, "geometry");
    scroll = new ListingScroll(1, geometry.visibleRows());
    scrollbar = new TerminalScrollbar(scroll, geometry.trackHeight());
  }

  public PromptListGeometry geometry() {
    return geometry;
  }

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

  public int indexOfRow(int row) {
    return scroll.firstIndex() + row;
  }

  public int indexAt(int windowX, int windowY) {
    final int row = geometry.rowAt(windowX, windowY);
    return row == PromptListGeometry.NO_ROW ? PromptListGeometry.NO_ROW : indexOfRow(row);
  }

  public boolean isOverScrollbar(int windowX, int windowY) {
    return geometry.isOverScrollbar(windowX, windowY);
  }

  public boolean grabScrollbar(int windowX, int windowY) {
    return geometry.isOverScrollbar(windowX, windowY) && scrollbar.grab(windowY - geometry.top());
  }

  public boolean isDraggingScrollbar() {
    return scrollbar.isDragging();
  }

  public void dragScrollbarTo(int windowY) {
    scrollbar.dragTo(windowY - geometry.top());
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
        originX + geometry.scrollbarX(),
        originY + geometry.top(),
        geometry.scrollbarWidth(),
        geometry.trackHeight());
  }
}
