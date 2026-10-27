package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.listing.ListingWindow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ListingPrefetchTest {
  private static final int TOTAL = 10_000;

  private static ListingScroll scrolledTo(int columns, int visibleRows, int topRow) {
    ListingScroll scroll = new ListingScroll(columns, visibleRows);
    scroll.fit(TOTAL);
    scroll.scrollTo(topRow);
    return scroll;
  }

  @DisplayName("the rows asked for pad the screen on both sides, stopping at the top")
  @ParameterizedTest(name = "{0}x{1} grid capped at {2}, top row {3}")
  @CsvSource({
    "9, 5, 128, 0, 0, 81",
    "9, 5, 128, 3, 0, 108",
    "9, 5, 128, 10, 54, 117",
    "1, 4, 64, 0, 0, 34",
    "1, 4, 64, 40, 10, 64"
  })
  void asksForABufferAroundTheVisibleRows(
      int columns,
      int visibleRows,
      int maxLength,
      int topRow,
      int expectedOffset,
      int expectedLength) {
    ListingPrefetch prefetch =
        new ListingPrefetch(scrolledTo(columns, visibleRows, topRow), maxLength);

    assertEquals(ListingWindow.of(expectedOffset, expectedLength), prefetch.window());
  }

  @DisplayName("the rows that must stay loaded are a thinner band than the rows asked for")
  @ParameterizedTest(name = "{0}x{1} grid capped at {2}, top row {3}")
  @CsvSource({
    "9, 5, 128, 0, 0, 63",
    "9, 5, 128, 3, 9, 81",
    "9, 5, 128, 10, 72, 81",
    "1, 4, 64, 0, 0, 19",
    "1, 4, 64, 40, 25, 34"
  })
  void needsAThinnerBandThanItAsksFor(
      int columns,
      int visibleRows,
      int maxLength,
      int topRow,
      int expectedOffset,
      int expectedLength) {
    ListingPrefetch prefetch =
        new ListingPrefetch(scrolledTo(columns, visibleRows, topRow), maxLength);

    assertEquals(ListingWindow.of(expectedOffset, expectedLength), prefetch.needed());
    assertTrue(prefetch.window().covers(prefetch.needed()));
  }

  @DisplayName("what is asked for never goes past what one reply may carry")
  @ParameterizedTest(name = "cap {0}")
  @ValueSource(ints = {45, 64, 100, 128, 1000})
  void staysInsideThePayloadCap(int maxLength) {
    ListingPrefetch prefetch = new ListingPrefetch(scrolledTo(9, 5, 50), maxLength);

    assertTrue(
        prefetch.window().length() <= Math.min(maxLength, ListingWindow.MAX_LENGTH),
        "asked for " + prefetch.window());
  }

  @DisplayName("a cap too small for any buffer still asks for the whole screen")
  @ParameterizedTest(name = "cap {0}")
  @ValueSource(ints = {0, 9, 45, 53})
  void aCapWithNoRoomToSpareAsksForJustTheScreen(int maxLength) {
    ListingPrefetch prefetch = new ListingPrefetch(scrolledTo(9, 5, 2), maxLength);

    assertEquals(ListingWindow.of(18, 45), prefetch.window());
    assertEquals(prefetch.window(), prefetch.needed());
  }

  @DisplayName("scrolling a little stays inside the rows already asked for")
  @ParameterizedTest(name = "{0}x{1} grid scrolled by {3} rows from row {2}")
  @CsvSource({"9, 5, 10, 1", "9, 5, 10, -1", "9, 5, 10, 2", "1, 4, 40, 3", "1, 4, 40, -15"})
  void aSmallScrollIsAlreadyLoaded(int columns, int visibleRows, int topRow, int scrolledBy) {
    ListingScroll scroll = scrolledTo(columns, visibleRows, topRow);
    ListingPrefetch prefetch = new ListingPrefetch(scroll, columns == 1 ? 64 : 128);
    ListingWindow asked = prefetch.window();

    scroll.scrollBy(scrolledBy);

    assertTrue(asked.covers(prefetch.needed()), "asked " + asked + " needed " + prefetch.needed());
  }

  @DisplayName("scrolling past the band already asked for needs a fresh request")
  @ParameterizedTest(name = "{0}x{1} grid scrolled by {3} rows from row {2}")
  @CsvSource({"9, 5, 10, 3", "9, 5, 10, -3", "1, 4, 40, 16", "1, 4, 40, -16"})
  void aLongScrollNeedsMoreRows(int columns, int visibleRows, int topRow, int scrolledBy) {
    ListingScroll scroll = scrolledTo(columns, visibleRows, topRow);
    ListingPrefetch prefetch = new ListingPrefetch(scroll, columns == 1 ? 64 : 128);
    ListingWindow asked = prefetch.window();

    scroll.scrollBy(scrolledBy);

    assertFalse(asked.covers(prefetch.needed()));
  }

  @Test
  void refusesToPrefetchForAScrollItWasNotGiven() {
    assertThrows(NullPointerException.class, () -> new ListingPrefetch(null, 128));
  }
}
