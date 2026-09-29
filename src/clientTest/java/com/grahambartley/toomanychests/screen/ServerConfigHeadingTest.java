package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ServerConfigHeadingTest {
  private static final int ENTRY_HEIGHT = ServerConfigList.ROW_HEIGHT - 4;

  @ParameterizedTest
  @ValueSource(ints = {0, 44, 200})
  void aHeadingSitsCloserToTheRowsBelowItThanToTheGroupAbove(int headingTop) {
    int textTop = ServerConfigHeading.textTop(headingTop, ENTRY_HEIGHT);
    int textBottom = textTop + ServerConfigEntry.LINE_HEIGHT;
    int previousRowTextBottom =
        headingTop
            - ServerConfigList.ROW_HEIGHT
            + ServerConfigRow.BOUNDS_TOP
            + ServerConfigEntry.LINE_HEIGHT;
    int nextRowTextTop = headingTop + ServerConfigList.ROW_HEIGHT + ServerConfigEntry.LABEL_TOP;

    int gapAbove = textTop - previousRowTextBottom;
    int gapBelow = nextRowTextTop - textBottom;

    assertTrue(gapBelow > 0, "the heading runs into its own first row");
    assertTrue(gapAbove > gapBelow, gapAbove + " above is not more than " + gapBelow + " below");
  }

  @Test
  void aHeadingStaysInsideItsOwnEntry() {
    int textTop = ServerConfigHeading.textTop(0, ENTRY_HEIGHT);

    assertTrue(textTop >= 0);
    assertTrue(textTop + ServerConfigEntry.LINE_HEIGHT <= ENTRY_HEIGHT);
  }
}
