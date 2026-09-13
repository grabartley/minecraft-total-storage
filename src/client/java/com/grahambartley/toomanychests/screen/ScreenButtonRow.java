package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ScreenButtonAnchor;

public final class ScreenButtonRow {
  public static final int SIZE = 14;
  public static final int GAP = 2;
  public static final int BAND_INSET_X = 8;
  public static final int BAND_INSET_Y = 3;
  public static final int OUTSIDE_GAP = 2;

  private static final int STEP = SIZE + GAP;

  private ScreenButtonRow() {}

  public static ScreenButtonSpot at(ScreenButtonAnchor anchor, ScreenPanel panel, int index) {
    return switch (anchor) {
      case INSIDE_TOP_RIGHT -> index == 0 ? inTheBand(panel) : downTheSide(panel, index - 1);
      case OUTSIDE_TOP_RIGHT -> downTheSide(panel, index);
      case OUTSIDE_BOTTOM_RIGHT -> upTheSide(panel, index);
    };
  }

  private static ScreenButtonSpot inTheBand(ScreenPanel panel) {
    return new ScreenButtonSpot(
        panel.x() + panel.width() - BAND_INSET_X - SIZE, panel.y() + BAND_INSET_Y);
  }

  private static ScreenButtonSpot downTheSide(ScreenPanel panel, int place) {
    return new ScreenButtonSpot(
        panel.x() + panel.width() + OUTSIDE_GAP, panel.y() + BAND_INSET_Y + place * STEP);
  }

  private static ScreenButtonSpot upTheSide(ScreenPanel panel, int place) {
    return new ScreenButtonSpot(
        panel.x() + panel.width() + OUTSIDE_GAP,
        panel.y() + panel.height() - BAND_INSET_Y - SIZE - place * STEP);
  }
}
