package com.grahambartley.toomanychests.listing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerminalListingMirrorTest {
  private static final int SYNC_ID = 7;
  private static final int OTHER_SYNC_ID = 8;

  /** Empty pages, because what is under test is which page is kept rather than what is in it. */
  private static ListingPage<ItemVariant> page(long revision) {
    return ListingPage.pending(revision);
  }

  @BeforeEach
  @AfterEach
  void closeAnyScreenLeftOpen() {
    TerminalListingMirror.closed();
  }

  @Test
  void nothingIsKeptWhileNoScreenIsOpen() {
    assertFalse(TerminalListingMirror.accept(SYNC_ID, page(1L)));
    assertFalse(TerminalListingMirror.isShowing(SYNC_ID));
  }

  @Test
  void anOpenScreenKeepsThePagesItAskedFor() {
    TerminalListingMirror.opened(SYNC_ID);

    assertTrue(TerminalListingMirror.accept(SYNC_ID, page(1L)));
    assertTrue(TerminalListingMirror.isShowing(SYNC_ID));
    assertEquals(1L, TerminalListingMirror.page().revision());
  }

  /**
   * A page can still be in flight when a screen closes and another opens. Without the sync id it
   * would be drawn into the new screen, which is showing a different terminal entirely.
   */
  @Test
  void aPageMeantForAnotherScreenIsNeverDrawnIntoThisOne() {
    TerminalListingMirror.opened(SYNC_ID);

    assertFalse(TerminalListingMirror.accept(OTHER_SYNC_ID, page(1L)));
    assertEquals(ListingRevision.NONE, TerminalListingMirror.page().revision());
  }

  @Test
  void aPageOvertakenByANewerOneIsStillDroppedWhileAScreenIsOpen() {
    TerminalListingMirror.opened(SYNC_ID);
    TerminalListingMirror.accept(SYNC_ID, page(5L));

    assertFalse(TerminalListingMirror.accept(SYNC_ID, page(4L)));
    assertEquals(5L, TerminalListingMirror.page().revision());
  }

  @Test
  void openingAScreenStartsFromTheBeginningRatherThanFromTheLastOnesPages() {
    TerminalListingMirror.opened(SYNC_ID);
    TerminalListingMirror.accept(SYNC_ID, page(9L));

    TerminalListingMirror.opened(OTHER_SYNC_ID);

    assertEquals(ListingRevision.NONE, TerminalListingMirror.page().revision());
    assertTrue(TerminalListingMirror.accept(OTHER_SYNC_ID, page(1L)));
  }

  @Test
  void closingAScreenLeavesNoPageBehindForTheNextOne() {
    TerminalListingMirror.opened(SYNC_ID);
    TerminalListingMirror.accept(SYNC_ID, page(3L));

    TerminalListingMirror.closed();

    assertFalse(TerminalListingMirror.isShowing(SYNC_ID));
    assertTrue(TerminalListingMirror.page().isEmpty());
    assertFalse(TerminalListingMirror.accept(SYNC_ID, page(4L)));
  }

  @Test
  void aPageThatIsNotThereIsNotKept() {
    TerminalListingMirror.opened(SYNC_ID);

    assertFalse(TerminalListingMirror.accept(SYNC_ID, null));
  }
}
