package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingWindow;

public final class ListingScroll {
  private final int columns;
  private final int visibleRows;

  private int totalRows;
  private int topRow;

  public ListingScroll(int columns, int visibleRows) {
    if (columns <= 0 || visibleRows <= 0) {
      throw new IllegalArgumentException(
          "a grid needs at least one column and one row: " + columns + "x" + visibleRows);
    }
    this.columns = columns;
    this.visibleRows = visibleRows;
  }

  public void fit(int totalMatching) {
    final long matching = Math.max(0, totalMatching);
    totalRows = (int) ((matching + columns - 1) / columns);
    topRow = Math.min(topRow, maxTopRow());
  }

  public int visibleRows() {
    return visibleRows;
  }

  public int totalRows() {
    return totalRows;
  }

  public int topRow() {
    return topRow;
  }

  public int maxTopRow() {
    return Math.max(0, totalRows - visibleRows);
  }

  public boolean overflows() {
    return maxTopRow() > 0;
  }

  public boolean scrollTo(int row) {
    final int clamped = Math.max(0, Math.min(maxTopRow(), row));
    if (clamped == topRow) {
      return false;
    }
    topRow = clamped;
    return true;
  }

  public boolean scrollBy(int rows) {
    return scrollTo(topRow + rows);
  }

  public int firstIndex() {
    return topRow * columns;
  }

  public ListingWindow window() {
    return ListingWindow.of(firstIndex(), visibleRows * columns);
  }
}
