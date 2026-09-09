package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalScrollbarTest {
  private static final int COLUMNS = 9;
  private static final int VISIBLE_ROWS = 5;
  private static final int TRACK = 100;

  private final ListingScroll scroll = new ListingScroll(COLUMNS, VISIBLE_ROWS);
  private final TerminalScrollbar scrollbar = new TerminalScrollbar(scroll, TRACK);

  private void aLongListing() {
    scroll.fit(50 * COLUMNS);
  }

  @Test
  void aListingThatFitsOnScreenHasNothingToGrab() {
    scroll.fit(VISIBLE_ROWS * COLUMNS);

    assertFalse(scrollbar.isEnabled());
    assertFalse(scrollbar.grab(0));
    assertFalse(scrollbar.isDragging());
  }

  @Test
  void aListingThatFitsOnScreenFillsItsTrack() {
    scroll.fit(VISIBLE_ROWS * COLUMNS);

    assertEquals(TRACK, scrollbar.thumbHeight());
    assertEquals(0, scrollbar.thumbTop());
  }

  @Test
  void aLongListingLeavesTheThumbRoomToTravel() {
    aLongListing();

    assertTrue(scrollbar.isEnabled());
    assertTrue(scrollbar.thumbHeight() < TRACK);
    assertEquals(0, scrollbar.thumbTop());
  }

  @Test
  void grabbingTheThumbWithoutMovingItLeavesTheGridWhereItWas() {
    aLongListing();
    scroll.scrollTo(20);
    int top = scrollbar.thumbTop();

    scrollbar.grab(top + 1);

    assertTrue(scrollbar.isDragging());
    assertEquals(20, scroll.topRow());
  }

  @Test
  void draggingTheThumbMovesTheGridWithIt() {
    aLongListing();
    scrollbar.grab(0);

    scrollbar.dragTo(TRACK - scrollbar.thumbHeight());

    assertEquals(scroll.maxTopRow(), scroll.topRow());
  }

  @Test
  void clickingTheTrackBesideTheThumbJumpsItUnderThePointer() {
    aLongListing();

    scrollbar.grab(TRACK - 1);

    assertTrue(scrollbar.isDragging());
    assertEquals(scroll.maxTopRow(), scroll.topRow());
  }

  @Test
  void aDragKeepsItsHoldOnTheThumbUntilItIsReleased() {
    aLongListing();
    scrollbar.grab(0);

    assertTrue(scrollbar.isDragging());

    scrollbar.release();

    assertFalse(scrollbar.isDragging());
  }

  @Test
  void aDragPastEitherEndOfTheTrackStopsAtThatEnd() {
    aLongListing();
    scrollbar.grab(0);

    scrollbar.dragTo(-500);
    assertEquals(0, scroll.topRow());

    scrollbar.dragTo(10_000);
    assertEquals(scroll.maxTopRow(), scroll.topRow());
  }

  @Test
  void aScrollbarWithNothingToScrollIsRefused() {
    assertThrows(NullPointerException.class, () -> new TerminalScrollbar(null, TRACK));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1, -100})
  void aTrackWithNoHeightIsRefused(int trackHeight) {
    assertThrows(IllegalArgumentException.class, () -> new TerminalScrollbar(scroll, trackHeight));
  }
}
