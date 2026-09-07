package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingWindow;

/**
 * How far down the listing the grid is looking, and which rows that means asking for.
 *
 * <p>Scrolling is counted in rows rather than in pixels or in cells. A row is what a wheel notch
 * moves, what a scrollbar step is worth, and what the grid draws a whole one of, so measuring
 * anything else would mean converting at every use and leaving the grid half a cell off the top.
 *
 * <p>The position is clamped against a total that changes underneath it. A network shrinking while
 * a player sits at the bottom of a long listing would otherwise leave the grid scrolled past the
 * end, showing nothing at all until they scrolled back up.
 */
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

  /** Takes the size of the whole result, pulling the view back inside it if it has shrunk. */
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

  /** The furthest down the grid may sit, which is the last row landing on the bottom of it. */
  public int maxTopRow() {
    return Math.max(0, totalRows - visibleRows);
  }

  /** Whether there is anything to scroll to, which is what greys the scrollbar out. */
  public boolean overflows() {
    return maxTopRow() > 0;
  }

  /** Returns whether the view actually moved, so a caller only asks the server when it did. */
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

  /** The first row of the whole result the grid is showing. */
  public int firstIndex() {
    return topRow * columns;
  }

  /**
   * The slice the grid is about to draw, which is exactly what it asks the server for.
   *
   * <p>Nothing beyond the grid is requested. Asking for a buffer either side would only pay off if
   * the request stayed the same across a scroll, and a window that moves with the view does not, so
   * the buffer would be fetched and thrown away every notch.
   */
  public ListingWindow window() {
    return ListingWindow.of(firstIndex(), visibleRows * columns);
  }
}
