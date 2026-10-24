package com.grahambartley.totalstorage.index;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NetworkIndexCountsTest {
  @ParameterizedTest
  @CsvSource({
    "0, 0, 0",
    "64, 128, 192",
    "9223372036854775807, 1, 9223372036854775807",
    "9223372036854775807, 9223372036854775807, 9223372036854775807",
    "9223372036854775806, 64, 9223372036854775807"
  })
  void additionSaturatesInsteadOfWrapping(long left, long right, long expected) {
    assertEquals(expected, NetworkIndexCounts.saturatedAdd(left, right));
  }
}
