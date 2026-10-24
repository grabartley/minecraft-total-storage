package com.grahambartley.totalstorage.lock;

public final class LockedSlotTint {
  public static final int NONE = 0;
  public static final int LOCKED = 0x60FFAA00;

  private LockedSlotTint() {}

  public static int colour(boolean locked) {
    return locked ? LOCKED : NONE;
  }

  public static boolean isPaintable(int colour) {
    return colour != NONE;
  }
}
