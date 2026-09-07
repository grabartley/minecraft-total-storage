package com.grahambartley.toomanychests.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.filter.ItemFilter;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TerminalListingStateTest {
  private final TerminalListingState state = new TerminalListingState();

  @Test
  void aScreenThatHasAskedForNothingIsShowingNothing() {
    assertTrue(state.window().isEmpty());
    assertFalse(state.hasWindow());
    assertSame(ItemFilter.NONE, state.filter());
    assertEquals(ListingRevision.NONE, state.revision());
  }

  @Test
  void whatAScreenAsksForIsRememberedForTheNextAnswer() {
    ItemFilter filter = ItemFilter.ofTags(TagCombinationMode.OR, Set.of("wood"));

    state.requested(ListingWindow.of(72, 72), filter);

    assertEquals(ListingWindow.of(72, 72), state.window());
    assertEquals(filter, state.filter());
    assertTrue(state.hasWindow());
  }

  /**
   * The criteria are kept rather than re-sent with every refresh, which is what lets the server
   * push a page to a player who is not touching anything.
   */
  @Test
  void aLaterRequestReplacesTheOneBeforeItRatherThanAddingToIt() {
    state.requested(ListingWindow.of(0, 72), ItemFilter.ofSearch("oak"));

    state.requested(ListingWindow.of(144, 72), ItemFilter.NONE);

    assertEquals(ListingWindow.of(144, 72), state.window());
    assertSame(ItemFilter.NONE, state.filter());
  }

  @Test
  void aRequestMissingEitherHalfFallsBackToAskingForNothingInParticular() {
    state.requested(null, null);

    assertEquals(ListingWindow.EMPTY, state.window());
    assertSame(ItemFilter.NONE, state.filter());
  }

  @Test
  void everyAnswerIsStampedNewerThanTheOneBeforeIt() {
    assertEquals(1L, state.nextRevision());
    assertEquals(2L, state.nextRevision());
    assertEquals(2L, state.revision());
  }

  /** A screen that has asked for nothing is not pushed to, because there is nothing to answer. */
  @Test
  void aWindowOfNoRowsIsNotAWindowWorthPushingTo() {
    state.requested(ListingWindow.of(40, 0), ItemFilter.NONE);

    assertFalse(state.hasWindow());
  }
}
