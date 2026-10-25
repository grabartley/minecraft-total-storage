package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class PromptListGeometryTest {
  private static final PromptListGeometry LIST = new PromptListGeometry(10, 20, 100, 12, 4, 114, 6);

  @Test
  void theTrackIsAsTallAsTheVisibleRows() {
    assertEquals(48, LIST.trackHeight());
  }

  @ParameterizedTest(name = "row {0}")
  @CsvSource({"0, 20", "1, 32", "3, 56"})
  void eachRowSitsOneRowHeightBelowThePrevious(int row, int expected) {
    assertEquals(expected, LIST.rowY(row));
  }

  @ParameterizedTest(name = "row {0}")
  @ValueSource(ints = {-1, 4})
  void askingForARowThatIsNotShownIsRefused(int row) {
    assertThrows(IndexOutOfBoundsException.class, () -> LIST.rowY(row));
  }

  @ParameterizedTest(name = "({0}, {1}) -> {2}")
  @CsvSource({
    "10, 20, 0",
    "109, 31, 0",
    "50, 32, 1",
    "109, 67, 3",
    "9, 20, -1",
    "110, 20, -1",
    "50, 19, -1",
    "50, 68, -1"
  })
  void aPointFindsTheRowUnderIt(int x, int y, int expected) {
    assertEquals(expected, LIST.rowAt(x, y));
  }

  @ParameterizedTest(name = "({0}, {1}) over={2}")
  @CsvSource({
    "114, 20, true",
    "119, 67, true",
    "113, 40, false",
    "120, 40, false",
    "116, 19, false",
    "116, 68, false"
  })
  void onlyPointsOnTheTrackAreOverTheScrollbar(int x, int y, boolean over) {
    assertEquals(over, LIST.isOverScrollbar(x, y));
  }
}
