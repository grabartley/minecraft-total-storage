package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DepositOutcomeTest {

  @ParameterizedTest
  @CsvSource({"64, 64, DEPOSITED", "64, 20, PARTIAL", "1, 1, DEPOSITED"})
  void whatMovedDecidesWhetherTheDepositWasWhole(long offered, long moved, DepositResult expected) {
    assertEquals(expected, DepositOutcome.stored(offered, moved).result());
  }

  /** What was not stored is still in the player's inventory, so it is reported rather than lost. */
  @Test
  void whatIsLeftOverIsWhatThePlayerIsStillHolding() {
    assertEquals(44L, DepositOutcome.stored(64L, 20L).remaining());
    assertEquals(0L, DepositOutcome.stored(64L, 64L).remaining());
    assertEquals(64L, DepositOutcome.noSpace(64L).remaining());
  }

  @Test
  void havingNothingToGiveIsToldApartFromHavingNowhereToPutIt() {
    assertEquals(DepositResult.NOTHING_TO_DEPOSIT, DepositOutcome.nothingToDeposit().result());
    assertEquals(DepositResult.NO_SPACE, DepositOutcome.noSpace(64L).result());
    assertEquals(0L, DepositOutcome.nothingToDeposit().offered());
    assertEquals(64L, DepositOutcome.noSpace(64L).offered());
  }

  @Test
  void aDepositThatStoredNothingHasToSayWhy() {
    assertThrows(IllegalArgumentException.class, () -> DepositOutcome.stored(64L, 0L));
  }

  @Test
  void aResultThatContradictsItsAmountsIsRefused() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new DepositOutcome(DepositResult.DEPOSITED, 64L, 0L, List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new DepositOutcome(DepositResult.NO_SPACE, 64L, 9L, List.of()));
    assertThrows(
        IllegalArgumentException.class,
        () -> new DepositOutcome(DepositResult.PARTIAL, 9L, 64L, List.of()));
  }
}
