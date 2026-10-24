package com.grahambartley.totalstorage.access;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrustEditOutcomeTest {
  @ParameterizedTest(name = "{0} changes the list: {1}")
  @CsvSource({
    "ADDED, true",
    "REMOVED, true",
    "ALREADY_TRUSTED, false",
    "NOT_TRUSTED, false",
    "UNKNOWN_PLAYER, false",
    "DENIED, false"
  })
  void everyOutcomeSaysWhetherTheListMoved(TrustEditOutcome outcome, boolean changed) {
    assertEquals(changed, outcome.changedTheList());
  }
}
