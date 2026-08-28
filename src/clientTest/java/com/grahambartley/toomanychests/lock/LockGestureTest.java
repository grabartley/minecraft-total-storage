package com.grahambartley.toomanychests.lock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LockGestureTest {
  private static final int RIGHT_BUTTON = 1;
  private static final int MIDDLE_BUTTON = 2;

  @Test
  void altWithTheLeftButtonTogglesALock() {
    assertTrue(LockGesture.isLockToggle(LockGesture.LEFT_BUTTON, true));
  }

  @Test
  void aPlainLeftClickStillMovesItems() {
    assertFalse(LockGesture.isLockToggle(LockGesture.LEFT_BUTTON, false));
  }

  @ParameterizedTest
  @ValueSource(ints = {RIGHT_BUTTON, MIDDLE_BUTTON, 3, 4})
  void noOtherButtonTogglesALockEvenWithAltHeld(int button) {
    assertFalse(LockGesture.isLockToggle(button, true));
  }

  @ParameterizedTest
  @ValueSource(ints = {RIGHT_BUTTON, MIDDLE_BUTTON, 3, 4})
  void everyOtherButtonKeepsItsVanillaMeaning(int button) {
    assertFalse(LockGesture.isLockToggle(button, false));
  }
}
