package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WithdrawOutcomeTest {

  @ParameterizedTest
  @CsvSource({"64, 64, WITHDRAWN", "64, 9, PARTIAL", "1, 1, WITHDRAWN"})
  void whatMovedDecidesWhetherTheWithdrawalWasWhole(
      long requested, long moved, WithdrawResult expected) {
    assertEquals(expected, WithdrawOutcome.moved(requested, moved).result());
  }

  @Test
  void theShortfallIsWhatThePlayerDidNotGet() {
    assertEquals(55L, WithdrawOutcome.moved(64L, 9L).shortfall());
    assertEquals(0L, WithdrawOutcome.moved(64L, 64L).shortfall());
  }

  @Test
  void aWithdrawalThatMovedNothingHasToSayWhy() {
    assertThrows(IllegalArgumentException.class, () -> WithdrawOutcome.moved(64L, 0L));
  }

  @Test
  void aRefusalCarriesNoAmounts() {
    WithdrawOutcome refused = WithdrawOutcome.refused();

    assertEquals(WithdrawResult.REFUSED, refused.result());
    assertEquals(0L, refused.requested());
    assertEquals(0L, refused.moved());
  }

  @ParameterizedTest
  @CsvSource({"NOTHING_AVAILABLE, 64", "NO_ROOM, 64"})
  void anEmptyHandedEndingStillRemembersWhatWasAskedFor(WithdrawResult result, long requested) {
    WithdrawOutcome outcome =
        result == WithdrawResult.NO_ROOM
            ? WithdrawOutcome.noRoom(requested)
            : WithdrawOutcome.nothingAvailable(requested);

    assertEquals(result, outcome.result());
    assertEquals(requested, outcome.shortfall());
  }

  /**
   * A result and its amounts that disagree would let a caller report a move that never happened.
   */
  @Test
  void aResultThatContradictsItsAmountsIsRefused() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new WithdrawOutcome(WithdrawResult.WITHDRAWN, 64L, 0L));
    assertThrows(
        IllegalArgumentException.class, () -> new WithdrawOutcome(WithdrawResult.REFUSED, 64L, 9L));
    assertThrows(
        IllegalArgumentException.class,
        () -> new WithdrawOutcome(WithdrawResult.WITHDRAWN, 9L, 64L));
  }
}
