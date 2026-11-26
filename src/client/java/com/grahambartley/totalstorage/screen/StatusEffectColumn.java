package com.grahambartley.totalstorage.screen;

import java.util.Optional;

public record StatusEffectColumn(int x, int y, int width, int height) {
  public static final int GAP = 2;
  public static final int NARROW_WIDTH = 32;
  public static final int WIDE_WIDTH = 120;
  public static final int ENTRY_HEIGHT = 32;
  public static final int ENTRY_STEP = 33;
  public static final int MOST_AT_FULL_STEP = 5;
  public static final int SQUEEZED_SPAN = 132;

  public static Optional<StatusEffectColumn> of(ScreenPanel panel, int screenWidth, int effects) {
    final int left = panel.x() + panel.width() + GAP;
    final int room = screenWidth - left;
    if (effects <= 0 || room < NARROW_WIDTH) {
      return Optional.empty();
    }
    final int step = effects > MOST_AT_FULL_STEP ? SQUEEZED_SPAN / (effects - 1) : ENTRY_STEP;
    return Optional.of(
        new StatusEffectColumn(
            left,
            panel.y(),
            room >= WIDE_WIDTH ? WIDE_WIDTH : NARROW_WIDTH,
            (effects - 1) * step + ENTRY_HEIGHT));
  }

  public boolean covers(ScreenButtonSpot spot) {
    return spot.x() < x + width
        && x < spot.x() + ScreenButtonRow.SIZE
        && spot.y() < y + height
        && y < spot.y() + ScreenButtonRow.SIZE;
  }
}
