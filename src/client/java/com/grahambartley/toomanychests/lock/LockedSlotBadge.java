package com.grahambartley.toomanychests.lock;

import com.grahambartley.toomanychests.screen.TerminalSprite;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;

public final class LockedSlotBadge {
  public static final int WIDTH = 7;
  public static final int HEIGHT = 8;

  private static final float ABOVE_THE_ITEM = 300.0f;

  private LockedSlotBadge() {}

  public static void draw(DrawContext context, Slot slot) {
    if (!LockedSlotState.isLocked(slot)) {
      return;
    }
    context.getMatrices().push();
    context.getMatrices().translate(0.0f, 0.0f, ABOVE_THE_ITEM);
    TerminalSprite.LOCKED_SLOT.draw(context, slot.x, slot.y, WIDTH, HEIGHT);
    context.getMatrices().pop();
  }
}
