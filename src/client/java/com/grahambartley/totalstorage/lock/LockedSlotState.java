package com.grahambartley.totalstorage.lock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;

public final class LockedSlotState {
  private LockedSlotState() {}

  public static boolean isLocked(Slot slot) {
    return isLocked(LockSlotTarget.lockableIndex(slot));
  }

  public static boolean isLocked(int inventorySlot) {
    final PlayerEntity player = MinecraftClient.getInstance().player;
    return player != null
        && LockedSlots.isValidSlot(inventorySlot)
        && LockedSlotsAttachment.get(player).isLocked(inventorySlot);
  }
}
