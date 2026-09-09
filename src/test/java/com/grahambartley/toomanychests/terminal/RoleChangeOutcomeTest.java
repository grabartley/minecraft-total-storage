package com.grahambartley.toomanychests.terminal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoleChangeOutcomeTest {
  @ParameterizedTest
  @CsvSource({
    "CHANGED, true",
    "UNCHANGED, false",
    "NOT_IN_NETWORK, false",
    "NO_TERMINAL, false",
    "DENIED, false"
  })
  void onlyAChangeCountsAsHavingWrittenAnything(RoleChangeOutcome outcome, boolean applied) {
    assertEquals(applied, outcome.applied());
  }
}
