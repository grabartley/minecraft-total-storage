package com.grahambartley.toomanychests.lock;

/**
 * The colour painted into a slot to show it is locked.
 *
 * <p>It goes behind the item rather than over it, so the stack, its count, and its durability bar
 * stay exactly as readable as they were. The badge drawn on top is what carries the meaning, so
 * this stays a wash rather than a fill: a player who cannot separate the amber from the slot loses
 * nothing, and a player who can gets the whole locked region at a glance.
 */
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
