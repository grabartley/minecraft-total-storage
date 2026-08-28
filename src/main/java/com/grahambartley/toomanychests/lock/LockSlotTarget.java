package com.grahambartley.toomanychests.lock;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;

/**
 * Maps a screen slot onto the inventory index a lock would cover.
 *
 * <p>A screen handler renumbers slots per screen, so the handler's own slot id is meaningless to a
 * lock. The backing inventory and its index are what stay stable, and armour and the offhand fall
 * out naturally because they sit above the lockable range.
 */
public final class LockSlotTarget {
  public static final int NONE = -1;

  private LockSlotTarget() {}

  public static int lockableIndex(Slot slot) {
    if (slot == null || !(slot.inventory instanceof PlayerInventory)) {
      return NONE;
    }
    final int index = slot.getIndex();
    return LockedSlots.isValidSlot(index) ? index : NONE;
  }

  public static boolean isLockable(Slot slot) {
    return lockableIndex(slot) != NONE;
  }
}
