package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.listing.ListingPage;
import com.grahambartley.totalstorage.listing.ListingStatus;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import java.util.List;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import org.junit.jupiter.api.Test;

class TerminalItemsPanelTest {

  private static ListingPage<ItemVariant> live(int totalMatching) {
    return new ListingPage<>(1L, ListingStatus.LIVE, 0, totalMatching, List.of());
  }

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
        panel.hoveredCell(TerminalScreenLayout.cellX(0) + 1, TerminalScreenLayout.cellY(0) + 1, 0));
  }

  @Test
  void aGridExplainingWhyItIsEmptyHasNoCellUnderThePointer() {
    TerminalItemsPanel panel = showing(ListingPage.offline(1L));

    assertEquals(
        TerminalScreenLayout.NO_CELL,
        panel.hoveredCell(TerminalScreenLayout.cellX(4) + 1, TerminalScreenLayout.cellY(4) + 1, 0));
  }

  @Test
  void aPointerBesideTheGridHasNoCellUnderIt() {
    TerminalItemsPanel panel = showing(live(500));

    assertEquals(
        TerminalScreenLayout.NO_CELL,
        panel.hoveredCell(TerminalScreenLayout.GRID_X - 1, TerminalScreenLayout.GRID_Y, 0));
    assertEquals(
        TerminalScreenLayout.NO_CELL,
        panel.hoveredCell(TerminalScreenLayout.GRID_X, TerminalScreenLayout.GRID_Y - 1, 0));
  }

  @Test
  void anEmptyCellOfALivePageIsStillUnderThePointer() {
    TerminalItemsPanel panel = showing(live(1));
    int lastCell = TerminalScreenLayout.LISTING_PAGE_SIZE - 1;

    assertNull(panel.rowAt(lastCell), "this test needs a cell with no item in it");
    assertEquals(
        lastCell,
        panel.hoveredCell(
            TerminalScreenLayout.cellX(lastCell) + 1, TerminalScreenLayout.cellY(lastCell) + 1, 0),
        "a cell with no item in it still highlights, the way a vanilla slot does");
  }

  @Test
  void aCellUnderTheNoticeBelongsToTheNoticeRatherThanTheGrid() {
    TerminalItemsPanel panel = showing(live(500));

    assertEquals(
        TerminalScreenLayout.NO_CELL,
        panel.hoveredCell(TerminalScreenLayout.cellX(0) + 1, TerminalScreenLayout.cellY(0) + 1, 1));
  }

  @Test
  void aNoticePushesTheGridDownSoEveryRowIsStillClickable() {
    TerminalItemsPanel panel = showing(live(500));
    final int firstVisible = TerminalScreenLayout.LISTING_COLUMNS;

    assertEquals(
        0,
        panel.hoveredCell(
            TerminalScreenLayout.cellX(firstVisible) + 1,
            TerminalScreenLayout.cellY(firstVisible) + 1,
            1));
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
