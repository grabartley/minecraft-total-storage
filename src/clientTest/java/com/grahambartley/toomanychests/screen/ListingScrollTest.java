package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.listing.ListingWindow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ListingScrollTest {
  private static final int COLUMNS = 9;
  private static final int VISIBLE_ROWS = 5;
  private static final int PAGE_SIZE = COLUMNS * VISIBLE_ROWS;

  private final ListingScroll scroll = new ListingScroll(COLUMNS, VISIBLE_ROWS);

  @Test
  void aFreshGridIsLookingAtTheTopOfNothing() {
    assertEquals(0, scroll.topRow());
    assertEquals(0, scroll.totalRows());
    assertFalse(scroll.overflows());
    assertEquals(ListingWindow.of(0, PAGE_SIZE), scroll.window());
  }

  @DisplayName("a part-full last row is still a row to scroll to")
  @ParameterizedTest(name = "{0} matches fill {1} rows")
  @CsvSource({"0, 0", "1, 1", "9, 1", "10, 2", "45, 5", "46, 6", "100, 12"})
  void countsThePartFullLastRowAsARow(int totalMatching, int expectedRows) {
    scroll.fit(totalMatching);

    assertEquals(expectedRows, scroll.totalRows());
  }

  @Test
  void aListingThatFitsOnScreenHasNowhereToScrollTo() {
    scroll.fit(PAGE_SIZE);

    assertFalse(scroll.overflows());
    assertEquals(0, scroll.maxTopRow());
    assertFalse(scroll.scrollBy(1));
  }

  @Test
  void theLastRowOfALongListingLandsOnTheBottomOfTheGrid() {
    scroll.fit(100);

    assertEquals(12 - VISIBLE_ROWS, scroll.maxTopRow());
    assertTrue(scroll.overflows());
  }

  @Test
  void scrollingMovesTheWindowItAsksFor() {
    scroll.fit(500);

    assertTrue(scroll.scrollBy(3));

    assertEquals(3, scroll.topRow());
    assertEquals(3 * COLUMNS, scroll.firstIndex());
    assertEquals(ListingWindow.of(3 * COLUMNS, PAGE_SIZE), scroll.window());
  }

  @DisplayName("a scroll that would leave the listing stops at its edge")
  @ParameterizedTest(name = "scrolling by {0} lands on row {1}")
  @CsvSource({"-1, 0", "0, 0", "3, 3", "1000, 51"})
  void clampsAScrollToTheEndsOfTheListing(int rows, int expectedTopRow) {
    scroll.fit(500);

    scroll.scrollBy(rows);

    assertEquals(expectedTopRow, scroll.topRow());
  }

  @Test
  void aScrollThatChangesNothingReportsThatItChangedNothing() {
    scroll.fit(500);
    scroll.scrollTo(4);

    assertFalse(scroll.scrollTo(4));
    assertTrue(scroll.scrollTo(5));
  }

  @Test
  void aListingThatShrinksPullsTheViewBackInsideIt() {
    scroll.fit(500);
    scroll.scrollTo(50);

    scroll.fit(45);

    assertEquals(0, scroll.topRow());
    assertEquals(ListingWindow.of(0, PAGE_SIZE), scroll.window());
  }

  @Test
  void aListingEmptiedEntirelyLeavesTheGridAtTheTop() {
    scroll.fit(500);
    scroll.scrollTo(20);

    scroll.fit(0);

    assertEquals(0, scroll.topRow());
    assertFalse(scroll.overflows());
  }

  @DisplayName("a total that could not have been counted leaves nothing to scroll through")
  @ParameterizedTest(name = "total {0}")
  @CsvSource({"-1", "-500", "-2147483648"})
  void aTotalThatCannotExistIsTreatedAsNothing(int totalMatching) {
    scroll.fit(totalMatching);

    assertEquals(0, scroll.totalRows());
    assertFalse(scroll.overflows());
  }

  @Test
  void anAbsurdlyLargeTotalStillCountsARealNumberOfRows() {
    scroll.fit(Integer.MAX_VALUE);

    assertTrue(scroll.totalRows() > 0, "got " + scroll.totalRows());
    assertTrue(scroll.maxTopRow() > 0);
  }

  @Test
  void aGridWithNoCellsIsRefusedRatherThanDividedBy() {
    assertThrows(IllegalArgumentException.class, () -> new ListingScroll(0, VISIBLE_ROWS));
    assertThrows(IllegalArgumentException.class, () -> new ListingScroll(COLUMNS, 0));
    assertThrows(IllegalArgumentException.class, () -> new ListingScroll(-1, -1));
  }
}
