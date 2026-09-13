package com.grahambartley.toomanychests.sort;

import net.minecraft.screen.slot.SlotActionType;

public final class MiddleClickSortGesture {
  private MiddleClickSortGesture() {}

  public static boolean isSortGesture(
      SlotActionType actionType, boolean creative, boolean enabled) {
    return enabled && !creative && actionType == SlotActionType.CLONE;
  }
}
