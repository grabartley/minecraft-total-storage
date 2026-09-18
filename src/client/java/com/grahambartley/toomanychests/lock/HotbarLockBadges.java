package com.grahambartley.toomanychests.lock;

import net.minecraft.client.gui.DrawContext;

public final class HotbarLockBadges {
  private HotbarLockBadges() {}

  public static void draw(DrawContext context) {
    final int top = HotbarSlotLayout.itemY(context.getScaledWindowHeight());
    for (int slot = 0; slot < HotbarSlotLayout.SLOTS; slot++) {
      if (!LockedSlotState.isLocked(slot)) {
        continue;
      }
      final int left = HotbarSlotLayout.itemX(context.getScaledWindowWidth(), slot);
      LockedSlotOverlay.drawAt(context, left, top);
      LockedSlotBadge.drawAt(context, left, top);
    }
  }
}
