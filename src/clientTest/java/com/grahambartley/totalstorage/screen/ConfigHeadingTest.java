package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigHeadingTest {
  private static final int ENTRY_HEIGHT = ConfigList.ROW_HEIGHT - 4;

  @ParameterizedTest
  @ValueSource(ints = {0, 44, 200})
  void aHeadingSitsCloserToTheRowsBelowItThanToTheGroupAbove(int headingTop) {
    int textTop = ConfigHeading.textTop(headingTop, ENTRY_HEIGHT);
    int textBottom = textTop + ConfigEntry.LINE_HEIGHT;
    int previousRowTextBottom =
        headingTop - ConfigList.ROW_HEIGHT + ConfigRow.BOUNDS_TOP + ConfigEntry.LINE_HEIGHT;
    int nextRowTextTop = headingTop + ConfigList.ROW_HEIGHT + ConfigEntry.LABEL_TOP;

    int gapAbove = textTop - previousRowTextBottom;
    int gapBelow = nextRowTextTop - textBottom;

    assertTrue(gapBelow > 0, "the heading runs into its own first row");
    assertTrue(gapAbove > gapBelow, gapAbove + " above is not more than " + gapBelow + " below");
  }

  @Test
  void aHeadingStaysInsideItsOwnEntry() {
    int textTop = ConfigHeading.textTop(0, ENTRY_HEIGHT);

    assertTrue(textTop >= 0);
    assertTrue(textTop + ConfigEntry.LINE_HEIGHT <= ENTRY_HEIGHT);
  }
}
