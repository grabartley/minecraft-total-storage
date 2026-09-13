package com.grahambartley.toomanychests.lock;

import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;

public final class LockedSlotOverlay {
  public static final int SLOT_SIZE = 16;

  private LockedSlotOverlay() {}

  public static void draw(DrawContext context, Slot slot) {
    if (!ClientPreferencesStore.current().lockedSlotTintVisible()
        || !LockedSlotState.isLocked(slot)) {
      return;
    }
    final int colour = LockedSlotTint.colour(true);
    if (LockedSlotTint.isPaintable(colour)) {
      context.fill(slot.x, slot.y, slot.x + SLOT_SIZE, slot.y + SLOT_SIZE, colour);
    }
  }
}
