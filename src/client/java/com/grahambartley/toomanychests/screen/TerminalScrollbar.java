package com.grahambartley.toomanychests.screen;

import java.util.Objects;

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
