package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ScrollbarMetricsTest {
  private static final int TRACK = 100;
  private static final int VISIBLE_ROWS = 5;

  static IntStream everyRowOfALongListing() {
    return IntStream.rangeClosed(0, 51);
  }

  @Test
  void aListingThatFitsOnScreenFillsTheWholeTrack() {
    assertEquals(TRACK, ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, VISIBLE_ROWS));
    assertEquals(TRACK, ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, 0));
  }

  @DisplayName("the thumb measures how much of the listing is on screen")
  @ParameterizedTest(name = "{0} rows of {1} visible fills {2} of the track")
  @CsvSource({"10, 5, 50", "20, 5, 25", "50, 5, 10"})
  void sizesTheThumbToTheShareOfTheListingOnScreen(int totalRows, int visibleRows, int expected) {
    assertEquals(expected, ScrollbarMetrics.thumbHeight(TRACK, visibleRows, totalRows));
  }

  /** Past this the art has no bevel left, and a thumb nobody can grab is not a scrollbar. */
  @Test
  void theThumbStopsShrinkingWhereItStopsBeingGrabbable() {
    assertEquals(
        ScrollbarMetrics.MIN_THUMB_HEIGHT, ScrollbarMetrics.thumbHeight(TRACK, 1, 100_000));
  }

  @Test
  void theThumbSitsAtTheTopOfATrackWithNowhereToScroll() {
    assertEquals(0, ScrollbarMetrics.thumbTop(TRACK, TRACK, 0, 0));
    assertEquals(0, ScrollbarMetrics.thumbTop(TRACK, 20, 7, 0));
  }

  @Test
  void theThumbTravelsFromTheTopOfTheTrackToTheBottomOfIt() {
    int height = ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, 50);

    assertEquals(0, ScrollbarMetrics.thumbTop(TRACK, height, 0, 45));
    assertEquals(TRACK - height, ScrollbarMetrics.thumbTop(TRACK, height, 45, 45));
  }

  @DisplayName("the thumb never leaves the track however far a caller claims to have scrolled")
  @ParameterizedTest(name = "row {0}")
  @CsvSource({"-100", "-1", "0", "45", "46", "100000"})
  void keepsTheThumbInsideTheTrack(int topRow) {
    int height = ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, 50);
    int top = ScrollbarMetrics.thumbTop(TRACK, height, topRow, 45);

    assertTrue(top >= 0 && top + height <= TRACK, "thumb top was " + top);
  }

  /**
   * A thumb dragged to the bottom of its travel has to reach the last row. Truncating leaves it one
   * row short at almost every track height, which reads as a scrollbar that will not go all the way
   * down.
   */
  @Test
  void aThumbDraggedToTheEndOfItsTravelReachesTheLastRow() {
    int height = ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, 56);

    assertEquals(51, ScrollbarMetrics.rowAt(TRACK, height, TRACK - height, 51));
    assertEquals(0, ScrollbarMetrics.rowAt(TRACK, height, 0, 51));
  }

  /**
   * A listing with no more rows than the track has pixels of travel has a pixel for every row, so
   * grabbing a thumb without moving it must not shift the grid a row.
   */
  @DisplayName("dragging the thumb to where a row put it lands back on that row")
  @ParameterizedTest(name = "row {0}")
  @MethodSource("everyRowOfALongListing")
  void everyRowSurvivesBeingDrawnAndDraggedBack(int topRow) {
    int height = ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, 56);
    int top = ScrollbarMetrics.thumbTop(TRACK, height, topRow, 51);

    assertEquals(topRow, ScrollbarMetrics.rowAt(TRACK, height, top, 51));
  }

  /**
   * Past a row per pixel the mapping is genuinely lossy, and the honest contract is that the
   * position stays near where the thumb is drawn rather than exact.
   */
  @Test
  void aListingWithMoreRowsThanPixelsStaysNearWhereTheThumbIsDrawn() {
    int maxTopRow = 5_000;
    int height = ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, maxTopRow + VISIBLE_ROWS);

    for (int row = 0; row <= maxTopRow; row += 97) {
      int top = ScrollbarMetrics.thumbTop(TRACK, height, row, maxTopRow);
      int back = ScrollbarMetrics.rowAt(TRACK, height, top, maxTopRow);

      assertTrue(
          Math.abs(back - row) <= maxTopRow / (TRACK - height),
          "row " + row + " came back as " + back);
    }
  }

  /** A listing far larger than a track must not wrap the pixel arithmetic into a negative. */
  @Test
  void anAbsurdlyLongListingStillMapsOntoItsTrack() {
    int maxTopRow = Integer.MAX_VALUE / 8;
    int height = ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, Integer.MAX_VALUE);

    assertEquals(0, ScrollbarMetrics.thumbTop(TRACK, height, 0, maxTopRow));
    assertEquals(TRACK - height, ScrollbarMetrics.thumbTop(TRACK, height, maxTopRow, maxTopRow));
    assertEquals(maxTopRow, ScrollbarMetrics.rowAt(TRACK, height, TRACK - height, maxTopRow));
  }

  @Test
  void aTrackWithNowhereToScrollAnswersEveryDragWithTheTopRow() {
    assertEquals(0, ScrollbarMetrics.rowAt(TRACK, TRACK, 40, 0));
    assertEquals(0, ScrollbarMetrics.rowAt(TRACK, TRACK, 40, 51));
  }

  @DisplayName("a drag past either end of the track stops at that end")
  @ParameterizedTest(name = "dragged to {0} lands on row {1}")
  @CsvSource({"-500, 0", "-1, 0", "10000, 51"})
  void clampsADragThatLeavesTheTrack(int thumbTop, int expectedRow) {
    int height = ScrollbarMetrics.thumbHeight(TRACK, VISIBLE_ROWS, 56);

    assertEquals(expectedRow, ScrollbarMetrics.rowAt(TRACK, height, thumbTop, 51));
  }
}
