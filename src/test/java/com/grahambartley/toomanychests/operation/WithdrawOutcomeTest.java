package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WithdrawOutcomeTest {
  @ParameterizedTest
  @CsvSource({"NO_STOCK, PARTIAL_NO_STOCK", "NO_ROOM, PARTIAL_NO_ROOM"})
  void aShortWithdrawalSaysWhichSideRanOut(ShortfallCause cause, WithdrawResult expected) {
    assertEquals(expected, WithdrawOutcome.partial(64L, 9L, cause).result());
  }

  @ParameterizedTest
  @CsvSource({"NO_STOCK, NOTHING_AVAILABLE", "NO_ROOM, NO_ROOM"})
  void anEmptyHandedWithdrawalSaysWhichSideRanOut(ShortfallCause cause, WithdrawResult expected) {
    assertEquals(expected, WithdrawOutcome.nothingMoved(64L, cause).result());
  }

  @Test
  void aWithdrawalThatMetItsRequestIsWholeAndNamesNoCause() {
    WithdrawOutcome outcome = WithdrawOutcome.filled(64L);

    assertEquals(WithdrawResult.WITHDRAWN, outcome.result());
    assertEquals(64L, outcome.moved());
    assertEquals(0L, outcome.shortfall());
  }

  @Test
  void theShortfallIsWhatThePlayerDidNotGet() {
    assertEquals(55L, WithdrawOutcome.partial(64L, 9L, ShortfallCause.NO_STOCK).shortfall());
    assertEquals(64L, WithdrawOutcome.nothingMoved(64L, ShortfallCause.NO_ROOM).shortfall());
  }

  @Test
  void aPartialWithdrawalHasToHaveMovedSomethingShortOfTheRequest() {
    assertThrows(
        IllegalArgumentException.class,
        () -> WithdrawOutcome.partial(64L, 0L, ShortfallCause.NO_STOCK));
    assertThrows(
        IllegalArgumentException.class,
        () -> WithdrawOutcome.partial(64L, 64L, ShortfallCause.NO_STOCK));
  }

  @Test
  void aShortfallCannotBeReportedWithoutACause() {
    assertThrows(NullPointerException.class, () -> WithdrawOutcome.nothingMoved(64L, null));
    assertThrows(NullPointerException.class, () -> WithdrawOutcome.partial(64L, 9L, null));
  }

  @Test
  void aRefusalCarriesNoAmounts() {
    WithdrawOutcome refused = WithdrawOutcome.refused();

    assertEquals(WithdrawResult.REFUSED, refused.result());
    assertEquals(0L, refused.requested());
    assertEquals(0L, refused.moved());
  }

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
