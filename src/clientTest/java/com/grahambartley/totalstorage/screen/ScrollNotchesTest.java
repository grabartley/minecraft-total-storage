package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ScrollNotchesTest {
  @Test
  void oneFirmScrollCoversSeveralRowsRatherThanOne() {
    assertEquals(ScrollNotches.ROWS_PER_NOTCH, ScrollNotches.rowsFor(-1.0));
    assertTrue(ScrollNotches.ROWS_PER_NOTCH > 1);
  }

  @Test
  void scrollingUpMovesTowardsTheTopOfTheList() {
    assertEquals(-ScrollNotches.ROWS_PER_NOTCH, ScrollNotches.rowsFor(1.0));
  }

  @ParameterizedTest
  @CsvSource({"1.0, 3", "2.0, 6", "3.0, 9", "0.4, 3"})
  void abiggerDeltaCoversMoreGroundAndASmallOneStillMoves(double delta, int expected) {
    assertEquals(expected, ScrollNotches.rowsFor(-delta));
  }

  @ParameterizedTest
  @ValueSource(doubles = {1.0, 4.0, 0.2})
  void theItemGridKeepsItsOwnOneRowPerNotchRate(double delta) {
    assertEquals((int) Math.max(1, Math.round(delta)), ScrollNotches.rowsFor(-delta, 1));
  }

  @Test
  void aScrollThatWentNowhereMovesNothing() {
    assertEquals(0, ScrollNotches.rowsFor(0.0));
    assertEquals(0, ScrollNotches.rowsFor(0.0, 1));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void aListThatAdvancesNoRowsPerNotchDoesNotScroll(int rowsPerNotch) {
    assertEquals(0, ScrollNotches.rowsFor(-1.0, rowsPerNotch));
  }
}
