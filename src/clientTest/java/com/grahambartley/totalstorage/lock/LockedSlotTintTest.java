package com.grahambartley.totalstorage.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LockedSlotTintTest {
  private static final int ALPHA_MASK = 0xFF000000;
  private static final int HALF_OPACITY = 128;

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
    int alpha = alphaOf(LockedSlotTint.LOCKED);

    assertTrue(
        alpha > 0 && alpha < 255, "Expected a partly transparent tint but alpha was " + alpha);
  }

  @Test
  void theTintStaysAWashSoTheBadgeRemainsTheIndicator() {
    int alpha = alphaOf(LockedSlotTint.LOCKED);

    assertTrue(
        alpha < HALF_OPACITY,
        "The tint reinforces the badge rather than carrying the lock state alone, so it must leave"
            + " most of the slot showing, but alpha was "
            + alpha);
  }

  private static int alphaOf(int colour) {
    return (colour & ALPHA_MASK) >>> 24;
  }
}
