package com.grahambartley.toomanychests.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ListingWindowTest {
  private static final List<String> TEN = List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j");

  @DisplayName("a window asked for inside the bounds is left exactly as it was asked for")
  @ParameterizedTest(name = "offset {0}, length {1}")
  @CsvSource({"0, 1", "0, 72", "40, 8", "1000, 128"})
  void keepsAWindowWithinTheBounds(int offset, int length) {
    ListingWindow window = ListingWindow.of(offset, length);

    assertEquals(offset, window.offset());
    assertEquals(length, window.length());
  }

  @DisplayName("a negative offset is a request for the start of the listing")
  @ParameterizedTest(name = "offset {0}")
  @ValueSource(ints = {-1, -72, Integer.MIN_VALUE})
  void raisesANegativeOffsetToTheStart(int offset) {
    assertEquals(0, ListingWindow.of(offset, 8).offset());
  }

  @DisplayName("a length outside the bounds is brought back inside them")
  @ParameterizedTest(name = "length {0} becomes {1}")
  @CsvSource({"-1, 0", "-500, 0", "129, 128", "100000, 128"})
  void clampsLengthToWhatAPageMayCarry(int requested, int expected) {
    assertEquals(expected, ListingWindow.of(0, requested).length());
  }

  @Test
  void theEndOfAWindowIsSaturatedRatherThanWrappedPastTheEndOfAnInt() {
    ListingWindow window = ListingWindow.of(Integer.MAX_VALUE, 128);

    assertEquals(Integer.MAX_VALUE, window.end());
  }

  @Test
  void aWindowOfNoLengthIsEmptyAndCoversNothing() {
    ListingWindow window = ListingWindow.of(5, 0);

    assertTrue(window.isEmpty());
    assertFalse(window.contains(5));
    assertEquals(List.of(), window.sliceOf(TEN));
  }

  @DisplayName("a window covers the rows from its offset up to but not past its end")
  @ParameterizedTest(name = "index {0} covered by offset 3 length 4: {1}")
  @CsvSource({"2, false", "3, true", "6, true", "7, false"})
  void coversTheRowsBetweenItsBounds(int index, boolean covered) {
    assertEquals(covered, ListingWindow.of(3, 4).contains(index));
  }

  @Test
  void slicingTakesTheRowsTheWindowCovers() {
    assertEquals(List.of("d", "e", "f"), ListingWindow.of(3, 3).sliceOf(TEN));
  }

  @Test
  void slicingPastTheEndTakesWhatIsLeftRatherThanFailing() {
    assertEquals(List.of("i", "j"), ListingWindow.of(8, 50).sliceOf(TEN));
  }

  /**
   * The results can shrink between a player scrolling and the request that scroll produced, so an
   * offset past the end is an ordinary empty page rather than a broken request.
   */
  @Test
  void slicingWhollyPastTheEndIsAnEmptyPage() {
    assertEquals(List.of(), ListingWindow.of(10, 8).sliceOf(TEN));
    assertEquals(List.of(), ListingWindow.of(500, 8).sliceOf(TEN));
  }

  @Test
  void slicingAnEmptyListIsAnEmptyPage() {
    assertEquals(List.of(), ListingWindow.of(0, 8).sliceOf(List.of()));
  }

  @Test
  void slicingRefusesAListItWasNotGiven() {
    assertThrows(NullPointerException.class, () -> ListingWindow.of(0, 8).sliceOf(null));
  }

  @Test
  void theEmptyWindowAsksForNothingFromTheStart() {
    assertEquals(0, ListingWindow.EMPTY.offset());
    assertTrue(ListingWindow.EMPTY.isEmpty());
  }
}
