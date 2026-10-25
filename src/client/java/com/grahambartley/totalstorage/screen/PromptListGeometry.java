package com.grahambartley.totalstorage.screen;

public record PromptListGeometry(
    int left,
    int top,
    int rowWidth,
    int rowHeight,
    int visibleRows,
    int scrollbarX,
    int scrollbarWidth) {
  public static final int NO_ROW = -1;

  public int trackHeight() {
    return rowHeight * visibleRows;
  }

  public int rowY(int row) {
    if (row < 0 || row >= visibleRows) {
      throw new IndexOutOfBoundsException("no prompt row at " + row);
    }
    return top + row * rowHeight;
  }

  public int rowAt(int windowX, int windowY) {
    if (windowX < left || windowX >= left + rowWidth) {
      return NO_ROW;
    }
    final int row = Math.floorDiv(windowY - top, rowHeight);
    return windowY < top || row >= visibleRows ? NO_ROW : row;
  }

  public boolean isOverScrollbar(int windowX, int windowY) {
    return windowX >= scrollbarX
        && windowX < scrollbarX + scrollbarWidth
        && windowY >= top
        && windowY < top + trackHeight();
  }
}
