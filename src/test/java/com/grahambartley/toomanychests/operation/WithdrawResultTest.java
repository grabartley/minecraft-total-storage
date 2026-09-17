package com.grahambartley.toomanychests.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WithdrawResultTest {
  @ParameterizedTest
  @CsvSource({
    "WITHDRAWN, true, false",
    "PARTIAL_NO_STOCK, true, true",
    "PARTIAL_NO_ROOM, true, true",
    "NOTHING_AVAILABLE, false, false",
    "NO_ROOM, false, false",
    "REFUSED, false, false"
  })
  void everyEndingKnowsWhetherItMovedAnythingAndWhetherItFellShort(
      WithdrawResult result, boolean moved, boolean partial) {
    assertEquals(moved, result.movedAnything());
    assertEquals(partial, result.isPartial());
  }
}
