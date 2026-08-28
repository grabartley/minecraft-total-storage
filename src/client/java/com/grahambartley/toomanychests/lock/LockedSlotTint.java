package com.grahambartley.toomanychests.lock;

/**
 * The colour painted into a slot to show it is locked.
 *
 * <p>It goes behind the item rather than over it, so the stack, its count, and its durability bar
 * stay exactly as readable as they were. That costs contrast against pale items, which is why a
 * badge is still owed on top of this.
 */
public final class LockedSlotTint {
  public static final int NONE = 0;
  public static final int LOCKED = 0xA0FFAA00;

  private LockedSlotTint() {}

  public static int colour(boolean locked) {
    return locked ? LOCKED : NONE;
  }

  public static boolean isPaintable(int colour) {
    return colour != NONE;
  }
}
