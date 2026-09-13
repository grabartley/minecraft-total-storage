package com.grahambartley.toomanychests.lock;

import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.Slot;

public final class LockedSlotOverlay {
  public static final int SLOT_SIZE = 16;

  private LockedSlotOverlay() {}

  public static void draw(DrawContext context, Slot slot) {
    final PlayerEntity player = MinecraftClient.getInstance().player;
    final int index = LockSlotTarget.lockableIndex(slot);
    if (player == null
        || index == LockSlotTarget.NONE
        || !ClientPreferencesStore.current().lockedSlotTintVisible()) {
      return;
    }
    final int colour = LockedSlotTint.colour(LockedSlotsAttachment.get(player).isLocked(index));
    if (LockedSlotTint.isPaintable(colour)) {
      context.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, colour);
    }
  }
}
