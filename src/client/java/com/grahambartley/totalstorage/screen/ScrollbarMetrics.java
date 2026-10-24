package com.grahambartley.totalstorage.screen;

public final class ScrollbarMetrics {
  public static final int MIN_THUMB_HEIGHT = 6;

  private ScrollbarMetrics() {}

  public static int thumbHeight(int trackHeight, int visibleRows, int totalRows) {
    if (totalRows <= visibleRows) {
      return trackHeight;
    }
    final int proportional = trackHeight * visibleRows / totalRows;
    return Math.max(MIN_THUMB_HEIGHT, Math.min(trackHeight, proportional));
  }

  public static int thumbTop(int trackHeight, int thumbHeight, int topRow, int maxTopRow) {
    if (maxTopRow <= 0) {
      return 0;
    }
    final long travel = Math.max(0, trackHeight - thumbHeight);
    final long row = Math.max(0, Math.min(maxTopRow, topRow));
    return (int) ((row * travel + maxTopRow / 2) / maxTopRow);
  }

  public static int rowAt(int trackHeight, int thumbHeight, int thumbTop, int maxTopRow) {
    final long travel = Math.max(0, trackHeight - thumbHeight);
    if (maxTopRow <= 0 || travel == 0L) {
      return 0;
    }
    final long clamped = Math.max(0L, Math.min(travel, thumbTop));
    return (int) ((clamped * maxTopRow + travel / 2L) / travel);
  }
}
