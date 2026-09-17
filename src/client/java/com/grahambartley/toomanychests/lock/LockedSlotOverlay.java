package com.grahambartley.toomanychests.lock;

import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;

public final class LockedSlotOverlay {
  public static final int SLOT_SIZE = 16;

  private LockedSlotOverlay() {}

  public static void draw(DrawContext context, Slot slot) {
    if (!LockedSlotState.isLocked(slot)) {
      return;
    }
    drawAt(context, slot.x, slot.y);
  }

  public static void drawAt(DrawContext context, int left, int top) {
    if (!ClientPreferencesStore.current().lockedSlotTintVisible()) {
      return;
    }
    final int colour = LockedSlotTint.colour(true);
    if (LockedSlotTint.isPaintable(colour)) {
      context.fill(left, top, left + SLOT_SIZE, top + SLOT_SIZE, colour);
    }
  }
}
