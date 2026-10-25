package com.grahambartley.totalstorage.lock;

import com.grahambartley.totalstorage.screen.TerminalSprite;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;

public final class LockedSlotBadge {
  public static final int WIDTH = 5;
  public static final int HEIGHT = 6;

  private static final float ABOVE_THE_ITEM = 300.0f;

  private LockedSlotBadge() {}

  public static void draw(DrawContext context, Slot slot) {
    if (!LockedSlotState.isLocked(slot)) {
      return;
    }
    drawAt(context, slot.x, slot.y);
  }

  public static void drawAt(DrawContext context, int left, int top) {
    context.getMatrices().push();
    context.getMatrices().translate(0.0f, 0.0f, ABOVE_THE_ITEM);
    drawFlat(context, left, top);
    context.getMatrices().pop();
  }

  public static void drawFlat(DrawContext context, int left, int top) {
    TerminalSprite.LOCKED_SLOT.draw(context, left, top, WIDTH, HEIGHT);
  }
}
