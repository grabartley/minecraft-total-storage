package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.screen.slot.SlotActionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class MiddleClickSortGestureTest {
  @Test
  void aSurvivalMiddleClickIsTheSortGesture() {
    assertTrue(MiddleClickSortGesture.isSortGesture(SlotActionType.CLONE, false, true));
  }

  @Test
  void creativeStackCloningKeepsItsVanillaMeaning() {
    assertFalse(MiddleClickSortGesture.isSortGesture(SlotActionType.CLONE, true, true));
  }

  @Test
  void aServerThatSwitchedTheGestureOffNeverReinterpretsTheClick() {
    assertFalse(MiddleClickSortGesture.isSortGesture(SlotActionType.CLONE, false, false));
  }

  @ParameterizedTest
  @EnumSource(
      value = SlotActionType.class,
      names = {"CLONE"},
      mode = EnumSource.Mode.EXCLUDE)
  void everyOtherClickIsLeftToVanilla(SlotActionType actionType) {
    assertFalse(MiddleClickSortGesture.isSortGesture(actionType, false, true));
  }
}
