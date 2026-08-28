package com.grahambartley.toomanychests.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LockedSlotTintTest {
  private static final int ALPHA_MASK = 0xFF000000;

  @Test
  void aLockedSlotIsPainted() {
    int colour = LockedSlotTint.colour(true);

    assertEquals(LockedSlotTint.LOCKED, colour);
    assertTrue(LockedSlotTint.isPaintable(colour));
  }

  @Test
  void anUnlockedSlotIsLeftAsVanillaDrewIt() {
    int colour = LockedSlotTint.colour(false);

    assertEquals(LockedSlotTint.NONE, colour);
    assertFalse(LockedSlotTint.isPaintable(colour));
  }

  @Test
  void theTintIsTranslucentSoTheSlotUnderneathStillReads() {
    int alpha = (LockedSlotTint.LOCKED & ALPHA_MASK) >>> 24;

    assertTrue(
        alpha > 0 && alpha < 255, "Expected a partly transparent tint but alpha was " + alpha);
  }
}
