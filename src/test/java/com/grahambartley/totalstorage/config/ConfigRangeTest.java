package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ConfigRangeTest {
  @ParameterizedTest
  @CsvSource({"1, 64, 0, 1", "1, 64, 65, 64", "1, 64, 16, 16"})
  void aValueOutsideTheRangeIsPulledBackToItsNearestEnd(int min, int max, int value, int clamped) {
    assertEquals(clamped, new ConfigRange(min, max).clamp(value));
  }

  @ParameterizedTest
  @CsvSource({"0, 100, 0, 0.0", "0, 100, 50, 0.5", "0, 100, 100, 1.0"})
  void aValueReadsAsItsShareOfTheRange(int min, int max, int value, double fraction) {
    assertEquals(fraction, new ConfigRange(min, max).fractionOf(value), 1.0e-9d);
  }

  @ParameterizedTest
  @CsvSource({"1, 64, 0.0, 1", "1, 64, 1.0, 64", "0, 100, 0.5, 50"})
  void aShareOfTheRangeReadsBackAsAValue(int min, int max, double fraction, int value) {
    assertEquals(value, new ConfigRange(min, max).valueAt(fraction));
  }

  @Test
  void aRangeWithNoWidthPutsEverySliderAtItsStart() {
    assertEquals(0.0d, new ConfigRange(5, 5).fractionOf(5), 1.0e-9d);
  }

  @Test
  void aRangeThatStartsAboveWhereItEndsIsRefused() {
    assertThrows(IllegalArgumentException.class, () -> new ConfigRange(10, 1));
  }
}
