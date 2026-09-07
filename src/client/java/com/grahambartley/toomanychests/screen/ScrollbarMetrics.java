package com.grahambartley.toomanychests.screen;

/**
 * The scrollbar in pixels: how tall the thumb is, where it sits, and what dragging it means.
 *
 * <p>Kept apart from {@link ListingScroll}, which counts rows. The thumb is the only part of
 * scrolling that has to think in pixels, and a scroll position that knew about a track height could
 * not be reasoned about without one.
 *
 * <p>The thumb is proportional to how much of the listing fits on screen, which is what makes it
 * read as a measure of the whole rather than as a button. It stops shrinking at {@link
 * #MIN_THUMB_HEIGHT} because past that the art has no bevel left and the thumb stops being
 * grabbable.
 */
public final class ScrollbarMetrics {
  /** The shortest the thumb sprite still draws as a thumb, per the sprite reference. */
  public static final int MIN_THUMB_HEIGHT = 6;

  private ScrollbarMetrics() {}

  public static int thumbHeight(int trackHeight, int visibleRows, int totalRows) {
    if (totalRows <= visibleRows) {
      return trackHeight;
    }
    final int proportional = trackHeight * visibleRows / totalRows;
    return Math.max(MIN_THUMB_HEIGHT, Math.min(trackHeight, proportional));
  }

  /**
   * How far down the track the thumb's top edge sits for a given scroll position.
   *
   * <p>Rounded to the nearest pixel rather than truncated, which is what makes this and {@link
   * #rowAt} inverses of each other. Truncating on the way out and rounding on the way back would
   * leave a thumb that jumped a row the instant a player touched it without moving.
   */
  public static int thumbTop(int trackHeight, int thumbHeight, int topRow, int maxTopRow) {
    if (maxTopRow <= 0) {
      return 0;
    }
    final long travel = Math.max(0, trackHeight - thumbHeight);
    final long row = Math.max(0, Math.min(maxTopRow, topRow));
    return (int) ((row * travel + maxTopRow / 2) / maxTopRow);
  }

  /**
   * Which row a thumb dragged to a given top edge means, rounded to the nearest one.
   *
   * <p>Rounding rather than truncating is what lets a thumb dragged to the bottom of its travel
   * reach the last row. Truncating leaves it one row short of the end at almost every track height,
   * which reads as a scrollbar that will not quite go all the way down.
   *
   * <p>A listing with more rows than the track has pixels cannot round-trip exactly, and does not
   * need to: several rows genuinely share a pixel, and the position the player sees is the one the
   * thumb is drawn at.
   */
  public static int rowAt(int trackHeight, int thumbHeight, int thumbTop, int maxTopRow) {
    final long travel = Math.max(0, trackHeight - thumbHeight);
    if (maxTopRow <= 0 || travel == 0L) {
      return 0;
    }
    final long clamped = Math.max(0L, Math.min(travel, thumbTop));
    return (int) ((clamped * maxTopRow + travel / 2L) / travel);
  }
}
