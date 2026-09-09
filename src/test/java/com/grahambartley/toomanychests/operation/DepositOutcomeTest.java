package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

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
