package com.grahambartley.totalstorage.operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class ShortfallCauseTest {
  @ParameterizedTest
  @CsvSource({"NO_STOCK, NOTHING_AVAILABLE, PARTIAL_NO_STOCK", "NO_ROOM, NO_ROOM, PARTIAL_NO_ROOM"})
  void eachCauseNamesTheEndingForBothAnEmptyHandedAndAShortWithdrawal(
      ShortfallCause cause, WithdrawResult emptyHanded, WithdrawResult partial) {
    assertEquals(emptyHanded, cause.emptyHanded());
    assertEquals(partial, cause.partial());
  }

  @ParameterizedTest
  @EnumSource(ShortfallCause.class)
  void aShortWithdrawalIsNeverReportedAsAnEmptyHandedOne(ShortfallCause cause) {
    assertNotEquals(cause.emptyHanded(), cause.partial());
    assertTrue(cause.partial().isPartial());
  }
}
