package com.grahambartley.toomanychests.lock;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;

public final class LockedSlotState {
  private LockedSlotState() {}

  public static boolean isLocked(Slot slot) {
    final PlayerEntity player = MinecraftClient.getInstance().player;
    final int index = LockSlotTarget.lockableIndex(slot);
    return player != null
        && index != LockSlotTarget.NONE
        && LockedSlotsAttachment.get(player).isLocked(index);
  }
}
