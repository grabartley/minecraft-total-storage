package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DepositResultTest {
  @ParameterizedTest
  @CsvSource({
    "DEPOSITED, true",
    "PARTIAL, true",
    "NOTHING_TO_DEPOSIT, false",
    "NO_SPACE, false",
    "REFUSED, false"
  })
  void onlyTheEndingsThatStoredSomethingCountAsHavingMovedItems(
      DepositResult result, boolean moved) {
    assertEquals(moved, result.movedAnything());
  }
}
