package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ScreenButtonAnchor;
import java.util.List;

public final class ScreenButtonBand {
  public static final int SLOT_SIZE = 18;

  private ScreenButtonBand() {}

  public static boolean isClear(ScreenPanel panel, List<ScreenButtonSpot> slots) {
    final ScreenButtonSpot band = ScreenButtonRow.at(ScreenButtonAnchor.INSIDE_TOP_RIGHT, panel, 0);
    return slots.stream().noneMatch(slot -> overlaps(band, slot));
  }

  private static boolean overlaps(ScreenButtonSpot band, ScreenButtonSpot slot) {
    return band.x() < slot.x() + SLOT_SIZE
        && slot.x() < band.x() + ScreenButtonRow.SIZE
        && band.y() < slot.y() + SLOT_SIZE
        && slot.y() < band.y() + ScreenButtonRow.SIZE;
  }
}
