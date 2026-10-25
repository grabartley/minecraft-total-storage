package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LockHintRingTest {
  @ParameterizedTest(name = "alt={0} lockable={1} locked={2}")
  @CsvSource({
    "true, true, false, true",
    "true, true, true, false",
    "true, false, false, false",
    "false, true, false, false",
    "false, false, false, false"
  })
  void onlyAnUnlockedLockableSlotIsRingedWhileAltIsHeld(
      boolean altDown, boolean lockable, boolean locked, boolean ringed) {
    assertEquals(ringed, LockHintRing.isRinged(altDown, lockable, locked));
  }

  @ParameterizedTest(name = "step {0}")
  @CsvSource({"0, true", "1, false", "2, true", "11, false"})
  void theRingIsDottedOnEveryOtherStep(int step, boolean dot) {
    assertEquals(dot, LockHintRing.isDot(step));
  }

  @Test
  void theRingIsInsetEquallyInsideTheSlot() {
    assertEquals(16, LockHintRing.SIZE + 2 * LockHintRing.INSET);
  }
}
