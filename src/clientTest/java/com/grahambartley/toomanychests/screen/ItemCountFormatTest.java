package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ItemCountFormatTest {
  /** Five characters is what a twenty pixel cell holds at the half scale the count is drawn at. */
  private static final int CELL_CHARACTER_BUDGET = 5;

  static LongStream everyOrderOfMagnitude() {
    return LongStream.of(
        0L,
        1L,
        9L,
        64L,
        999L,
        1_000L,
        9_999L,
        10_000L,
        99_999L,
        100_000L,
        999_999L,
        1_000_000L,
        999_999_999L,
        1_000_000_000L,
        999_999_999_999L);
  }

  @DisplayName("a count that fits a cell is written out in full")
  @ParameterizedTest(name = "{0} reads as {1}")
  @CsvSource({
    "0, 0",
    "1, 1",
    "64, 64",
    "999, 999",
    "1000, '1,000'",
    "1024, '1,024'",
    "9999, '9,999'"
  })
  void writesACountThatFitsInFull(long count, String expected) {
    assertEquals(expected, ItemCountFormat.cell(count));
  }

  @DisplayName("a count too wide for a cell is compacted rather than clipped")
  @ParameterizedTest(name = "{0} reads as {1}")
  @CsvSource({
    "10000, 10.0k",
    "12345, 12.3k",
    "99999, 99.9k",
    "100000, 100k",
    "999999, 999k",
    "1000000, 1.0M",
    "1234567, 1.2M",
    "1000000000, 1.0B",
    "1000000000000, 1.0T",
  })
  void compactsACountThatWouldOverflowACell(long count, String expected) {
    assertEquals(expected, ItemCountFormat.cell(count));
  }

  /**
   * A storage display that rounds up promises items that are not there. Understating is the safe
   * direction: a player who sees {@code 12.3k} and finds 12,399 is never caught short.
   */
  @Test
  void compactionNeverClaimsMoreThanTheNetworkHolds() {
    assertEquals("12.3k", ItemCountFormat.cell(12_399L));
    assertEquals("999k", ItemCountFormat.cell(999_999L));
  }

  @DisplayName("every count a network could reach still fits its cell")
  @ParameterizedTest(name = "{0}")
  @MethodSource("everyOrderOfMagnitude")
  void everyCountFitsTheCellItIsDrawnIn(long count) {
    String label = ItemCountFormat.cell(count);

    assertTrue(
        label.length() <= CELL_CHARACTER_BUDGET,
        count + " reads as '" + label + "', which is " + label.length() + " characters");
  }

  @Test
  void theTooltipShowsTheRealFigureRatherThanTheOneThatFitted() {
    assertEquals("1,234,567", ItemCountFormat.exact(1_234_567L));
    assertEquals("12,399", ItemCountFormat.exact(12_399L));
    assertEquals("7", ItemCountFormat.exact(7L));
  }

  /** The grouping has to read the same for every player, not follow whatever locale they run in. */
  @Test
  void groupingDoesNotChangeWithTheDefaultLocale() {
    java.util.Locale original = java.util.Locale.getDefault();
    try {
      java.util.Locale.setDefault(java.util.Locale.GERMANY);

      assertEquals("1,234,567", ItemCountFormat.exact(1_234_567L));
    } finally {
      java.util.Locale.setDefault(original);
    }
  }
}
