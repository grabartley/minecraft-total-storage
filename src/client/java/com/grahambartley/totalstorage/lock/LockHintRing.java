package com.grahambartley.totalstorage.lock;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.screen.slot.Slot;

public final class LockHintRing {
  public static final int INSET = 2;

  public static final int SIZE = 16 - 2 * INSET;

  public static final int COLOUR = 0x8CFFFFFF;

  private static final float ABOVE_THE_ITEM = 250.0f;

  private LockHintRing() {}

  public static boolean isRinged(boolean altDown, boolean lockable, boolean locked) {
    return altDown && lockable && !locked;
  }

  public static boolean isDot(int step) {
    return step % 2 == 0;
  }

  public static void draw(DrawContext context, Slot slot, boolean altDown) {
    if (!isRinged(altDown, LockSlotTarget.isLockable(slot), LockedSlotState.isLocked(slot))) {
      return;
    }
    final int left = slot.x + INSET;
    final int top = slot.y + INSET;
    final int last = SIZE - 1;
    context.getMatrices().push();
    context.getMatrices().translate(0.0f, 0.0f, ABOVE_THE_ITEM);
    for (int step = 0; step < SIZE; step++) {
      if (!isDot(step)) {
        continue;
      }
      dot(context, left + step, top);
      dot(context, left + last - step, top + last);
      dot(context, left, top + last - step);
      dot(context, left + last, top + step);
    }
    context.getMatrices().pop();
  }

  private static void dot(DrawContext context, int x, int y) {
    context.fill(x, y, x + 1, y + 1, COLOUR);
  }
}
