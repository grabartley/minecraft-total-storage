package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.listing.ListingPage;
import com.grahambartley.toomanychests.listing.ListingStatus;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import org.junit.jupiter.api.Test;

/**
 * Every way the item grid answers that a cell holds nothing.
 *
 * <p>A cell holding a row needs an {@code ItemVariant} and therefore a registry, so what a page
 * really contains is left to {@code ListingPageTest} and to the terminal's automated QA. What is
 * reachable here is the half that decides a cell is empty, and that half is the one a click has to
 * get right: a grid drawing a placeholder must not report a cell under the pointer, or clicking the
 * "this network is empty" message would withdraw something.
 */
class TerminalItemsPanelTest {

  private static TerminalItemsPanel showing(ListingPage<ItemVariant> page) {
    TerminalItemsPanel panel = new TerminalItemsPanel();
    panel.beginFrame(page);
    return panel;
  }

  @Test
  void aScreenThatHasHeardNothingYetHasNoCellUnderThePointer() {
    TerminalItemsPanel panel = showing(ListingPage.pending(0L));

    assertEquals(
        TerminalScreenLayout.NO_CELL,
        panel.hoveredCell(TerminalScreenLayout.cellX(0) + 1, TerminalScreenLayout.cellY(0) + 1));
  }

  /** The empty-state picture is drawn where the cells would be, and must not be clickable. */
  @Test
  void aGridExplainingWhyItIsEmptyHasNoCellUnderThePointer() {
    TerminalItemsPanel panel = showing(ListingPage.offline(1L));

    assertEquals(
        TerminalScreenLayout.NO_CELL,
        panel.hoveredCell(TerminalScreenLayout.cellX(4) + 1, TerminalScreenLayout.cellY(4) + 1));
  }

  @Test
  void aPointerBesideTheGridHasNoCellUnderIt() {
    TerminalItemsPanel panel = showing(ListingPage.pending(0L));

    assertEquals(
        TerminalScreenLayout.NO_CELL,
        panel.hoveredCell(TerminalScreenLayout.GRID_X - 1, TerminalScreenLayout.GRID_Y));
  }

  @Test
  void aCellOfAPageWithNoRowsHoldsNothing() {
    TerminalItemsPanel panel = showing(ListingPage.pending(0L));

    assertNull(panel.rowAt(0));
    assertNull(panel.rowAt(TerminalScreenLayout.LISTING_PAGE_SIZE - 1));
  }

  @Test
  void thePageAFrameDrawsFromIsTheOneItWasHanded() {
    ListingPage<ItemVariant> offline = ListingPage.offline(3L);

    assertEquals(offline, showing(offline).page());
  }

  /** A listing that shrank under a scrolled grid must not leave it pointing past the end. */
  @Test
  void theScrollIsBroughtBackInsideAListingThatShrank() {
    TerminalItemsPanel panel = new TerminalItemsPanel();
    panel.beginFrame(new ListingPage<>(1L, ListingStatus.LIVE, 0, 500, List.of()));
    panel.scroll().scrollBy(4);
    assertTrue(panel.scroll().firstIndex() > 0);

    panel.beginFrame(ListingPage.offline(2L));

    assertEquals(0, panel.scroll().firstIndex());
  }
}
