package com.grahambartley.toomanychests.lock;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;

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
