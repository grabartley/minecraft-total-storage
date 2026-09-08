package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * What a deposit is allowed to claim about itself.
 *
 * <p>An outcome that came up short has to account for the shortfall by item, and naming an item
 * needs a registry, so the endings that carry leftovers are built in {@code DepositServiceGameTest}
 * against a real inventory. What is left here is every rule that can be broken without one, which
 * is the set of contradictions a record must never hold: storing more than was offered, reporting a
 * result its own amounts disagree with, and a shortfall nothing accounts for.
 */
class DepositOutcomeTest {

  @Test
  void aWholeDepositLeavesNothingBehindAndSaysSo() {
    DepositOutcome whole = DepositOutcome.stored(64L, 64L, List.of());

    assertEquals(DepositResult.DEPOSITED, whole.result());
    assertEquals(0L, whole.remaining());
    assertEquals(0, whole.leftoverKinds());
  }

  @Test
  void havingNothingToGiveIsToldApartFromHavingNowhereToPutIt() {
    assertEquals(DepositResult.NOTHING_TO_DEPOSIT, DepositOutcome.nothingToDeposit().result());
    assertEquals(0L, DepositOutcome.nothingToDeposit().offered());
    assertEquals(DepositResult.SLOT_LOCKED, DepositOutcome.slotLocked().result());
    assertEquals(DepositResult.RATE_LIMITED, DepositOutcome.rateLimited().result());
  }

  @Test
  void aDepositThatStoredNothingHasToSayWhy() {
    assertThrows(IllegalArgumentException.class, () -> DepositOutcome.stored(64L, 0L, List.of()));
  }

  /**
   * A shortfall nobody accounts for is the way this record could quietly go back to reporting a
   * bare total, so it is refused rather than drawn.
   */
  @Test
  void aShortfallWithNothingToAccountForItIsRefused() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new DepositOutcome(DepositResult.PARTIAL, 64L, 20L, List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new DepositOutcome(DepositResult.NO_SPACE, 64L, 0L, List.of()));
  }

  @Test
  void aResultThatContradictsItsAmountsIsRefused() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new DepositOutcome(DepositResult.DEPOSITED, 64L, 0L, List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new DepositOutcome(DepositResult.PARTIAL, 9L, 64L, List.of()));
  }

  @Test
  void aNegativeAmountIsRefusedRatherThanClamped() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new DepositOutcome(DepositResult.NOTHING_TO_DEPOSIT, -1L, 0L, List.of()));
  }
}
