package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SortFeedbackTest {
  @ParameterizedTest
  @EnumSource(SortOutcome.class)
  void everyOutcomeSaysSomethingBackToThePlayer(SortOutcome outcome) {
    assertNotNull(SortFeedback.messageFor(outcome));
  }

  @Test
  void anOutcomeTheReaderMustTellApartReadsDifferently() {
    final Set<String> wordings = new HashSet<>();
    for (final SortOutcome outcome : SortOutcome.values()) {
      wordings.add(SortFeedback.messageFor(outcome).getString());
    }

    assertEquals(SortOutcome.values().length - 1, wordings.size());
  }

  @Test
  void aScreenThatCannotBeSortedReadsTheSameAsNoScreenAtAll() {
    assertEquals(
        SortFeedback.messageFor(SortOutcome.NO_OPEN_SCREEN).getString(),
        SortFeedback.messageFor(SortOutcome.SCREEN_NOT_SORTABLE).getString());
  }
}
