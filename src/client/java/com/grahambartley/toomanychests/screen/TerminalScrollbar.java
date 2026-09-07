package com.grahambartley.toomanychests.screen;

import java.util.Objects;

/**
 * The scrollbar a player grabs: where its thumb is, and what dragging it does to the grid.
 *
 * <p>Measured from the top of its own track rather than in window coordinates, so nothing here
 * needs to know where the window was drawn. The screen converts once at the edge, and everything
 * this decides can be proven without one.
 *
 * <p>Owns the drag because a drag is state that outlives the click that began it. Holding it on the
 * screen alongside the chassis, the grid and the tooltip is what turns a screen into a class that
 * grew sideways.
 */
public final class TerminalScrollbar {
  private final ListingScroll scroll;
  private final int trackHeight;

  private boolean dragging;
  private int grabOffset;

  public TerminalScrollbar(ListingScroll scroll, int trackHeight) {
    this.scroll = Objects.requireNonNull(scroll, "scroll");
    if (trackHeight <= 0) {
      throw new IllegalArgumentException("a track needs a height: " + trackHeight);
    }
    this.trackHeight = trackHeight;
  }

  /** Whether there is anything to scroll to, which is what greys the thumb out. */
  public boolean isEnabled() {
    return scroll.overflows();
  }

  public boolean isDragging() {
    return dragging;
  }

  public int thumbHeight() {
    return ScrollbarMetrics.thumbHeight(trackHeight, scroll.visibleRows(), scroll.totalRows());
  }

  public int thumbTop() {
    return ScrollbarMetrics.thumbTop(
        trackHeight, thumbHeight(), scroll.topRow(), scroll.maxTopRow());
  }

  /**
   * Takes hold of the thumb where it was clicked, or jumps it under the pointer when the track was
   * clicked beside it. Grabbing by the offset is what stops the thumb snapping its own centre to
   * the pointer the instant a player touches it without moving.
   *
   * @return whether the click was taken, which is false on a track with nowhere to scroll
   */
  public boolean grab(int offsetInTrack) {
    if (!isEnabled()) {
      return false;
    }
    dragging = true;
    final int height = thumbHeight();
    final int top = thumbTop();
    if (offsetInTrack >= top && offsetInTrack < top + height) {
      grabOffset = offsetInTrack - top;
      return true;
    }
    grabOffset = height / 2;
    dragTo(offsetInTrack);
    return true;
  }

  public void dragTo(int offsetInTrack) {
    scroll.scrollTo(
        ScrollbarMetrics.rowAt(
            trackHeight, thumbHeight(), offsetInTrack - grabOffset, scroll.maxTopRow()));
  }

  public void release() {
    dragging = false;
  }
}
