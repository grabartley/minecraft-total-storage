package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WithdrawResultTest {
  @ParameterizedTest
  @CsvSource({
    "WITHDRAWN, true",
    "PARTIAL, true",
    "NOTHING_AVAILABLE, false",
    "NO_ROOM, false",
    "REFUSED, false"
  })
  void onlyTheEndingsThatDeliveredSomethingCountAsHavingMovedItems(
      WithdrawResult result, boolean moved) {
    assertEquals(moved, result.movedAnything());
  }
}
