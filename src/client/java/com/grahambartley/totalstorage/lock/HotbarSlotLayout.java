package com.grahambartley.totalstorage.lock;

public final class HotbarSlotLayout {
  public static final int SLOTS = 9;

  private static final int HALF_BAR_WIDTH = 90;
  private static final int SLOT_PITCH = 20;
  private static final int ITEM_INSET_X = 2;
  private static final int BOTTOM_GAP = 16 + 3;

  private HotbarSlotLayout() {}

  public static int itemX(int scaledWindowWidth, int slot) {
    requireSlot(slot);
    return scaledWindowWidth / 2 - HALF_BAR_WIDTH + slot * SLOT_PITCH + ITEM_INSET_X;
  }

  public static int itemY(int scaledWindowHeight) {
    return scaledWindowHeight - BOTTOM_GAP;
  }

  private static void requireSlot(int slot) {
    if (slot < 0 || slot >= SLOTS) {
      throw new IndexOutOfBoundsException("no hotbar slot at " + slot);
    }
  }
}
