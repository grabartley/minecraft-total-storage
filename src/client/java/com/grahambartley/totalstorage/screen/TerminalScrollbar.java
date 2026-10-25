package com.grahambartley.totalstorage.screen;

import java.util.Objects;

public final class TerminalScrollbar {
  private final ListingScroll scroll;
  private final int trackHeight;
  private final int fixedThumbHeight;

  private boolean dragging;
  private int grabOffset;

  public TerminalScrollbar(ListingScroll scroll, int trackHeight) {
    this(scroll, trackHeight, 0);
  }

  public TerminalScrollbar(ListingScroll scroll, int trackHeight, int fixedThumbHeight) {
    this.scroll = Objects.requireNonNull(scroll, "scroll");
    if (trackHeight <= 0) {
      throw new IllegalArgumentException("a track needs a height: " + trackHeight);
    }
    if (fixedThumbHeight < 0 || fixedThumbHeight > trackHeight) {
      throw new IllegalArgumentException(
          "a thumb of " + fixedThumbHeight + " does not fit a track of " + trackHeight);
    }
    this.trackHeight = trackHeight;
    this.fixedThumbHeight = fixedThumbHeight;
  }

  public boolean isEnabled() {
    return scroll.overflows();
  }

  public boolean isDragging() {
    return dragging;
  }

  public int thumbHeight() {
    if (fixedThumbHeight > 0) {
      return fixedThumbHeight;
    }
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
