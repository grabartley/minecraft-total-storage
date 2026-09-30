package com.grahambartley.toomanychests.access;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TrustEditOutcomeTest {
  @ParameterizedTest(name = "{0} changes the list: {1}, keeps the typed name: {2}")
  @CsvSource({
    "ADDED, true, false",
    "REMOVED, true, false",
    "ALREADY_TRUSTED, false, true",
    "NOT_TRUSTED, false, false",
    "UNKNOWN_PLAYER, false, true",
    "DENIED, false, false"
  })
  void everyOutcomeSaysWhetherTheListMovedAndWhetherThePromptKeepsTheName(
      TrustEditOutcome outcome, boolean changed, boolean keeps) {
    assertEquals(changed, outcome.changedTheList());
    assertEquals(keeps, outcome.keepsTheTypedName());
  }
}
