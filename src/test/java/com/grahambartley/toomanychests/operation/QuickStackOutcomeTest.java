package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class QuickStackOutcomeTest {
  @Test
  void aStackedOutcomeCarriesWhatMovedAndWhereItWent() {
    QuickStackOutcome outcome = QuickStackOutcome.stacked(48L, 3);

    assertEquals(QuickStackResult.STACKED, outcome.result());
    assertEquals(48L, outcome.moved());
    assertEquals(3, outcome.containersTouched());
  }

  @ParameterizedTest
  @CsvSource({"REFUSED", "RATE_LIMITED", "NOTHING_TO_STACK"})
  void anOutcomeThatMovedNothingCountsNothing(String name) {
    QuickStackOutcome outcome =
        switch (QuickStackResult.valueOf(name)) {
          case REFUSED -> QuickStackOutcome.refused();
          case RATE_LIMITED -> QuickStackOutcome.rateLimited();
          default -> QuickStackOutcome.nothingToStack();
        };

    assertEquals(0L, outcome.moved());
    assertEquals(0, outcome.containersTouched());
  }

  @Test
  void aResultThatMovedNothingCannotClaimItemsMoved() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new QuickStackOutcome(QuickStackResult.NOTHING_TO_STACK, 5L, 1));
  }

  @Test
  void aStackedResultThatMovedNothingIsRefused() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new QuickStackOutcome(QuickStackResult.STACKED, 0L, 1));
  }

  @Test
  void itemsCannotMoveIntoNoContainerAtAll() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new QuickStackOutcome(QuickStackResult.STACKED, 5L, 0));
  }

  @Test
  void anOutcomeCannotCountBackwards() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new QuickStackOutcome(QuickStackResult.NOTHING_TO_STACK, -1L, 0));
  }

  @Test
  void anOutcomeNeedsAResult() {
    assertThrows(NullPointerException.class, () -> new QuickStackOutcome(null, 0L, 0));
  }
}
