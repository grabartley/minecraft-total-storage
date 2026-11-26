package com.grahambartley.totalstorage.lock;

import com.grahambartley.totalstorage.config.ClientPreferencesStore;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

public final class LockedSlotOverlay {
  public static final int SLOT_SIZE = 16;

  private LockedSlotOverlay() {}

  public static void draw(DrawContext context, Screen screen, boolean locked, int left, int top) {
    if (!LockCues.show(screen, locked)) {
      return;
    }
    drawAt(context, left, top);
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
