package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.filter.TagCombinationMode;
import com.grahambartley.totalstorage.listing.ListingWindow;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TerminalRequestsTest {
  private static final int SYNC_ID = 7;

  private final List<String> sent = new ArrayList<>();

  private final TerminalRequests requests =
      new TerminalRequests(
          SYNC_ID,
          new TerminalRequestSender() {
            @Override
            public void rows(
                int syncId,
                ListingWindow window,
                String search,
                TagCombinationMode mode,
                Set<String> tags) {
              sent.add("rows " + syncId + " " + window.offset() + "," + window.length());
            }

            @Override
            public void settingsShowing(int syncId, int offset, int length) {
              sent.add("showing " + syncId + " " + offset + "," + length);
            }

            @Override
            public void settingsClosed(int syncId) {
              sent.add("closed " + syncId);
            }
          });

  @Test
  void aSectionThatWantsNoRowsStillTellsTheServerThePanelIsShowing() {
    requests.showSettings(ListingWindow.EMPTY);

    assertEquals(List.of("showing 7 0,0"), sent);
    assertTrue(requests.isSettingsShowing());
  }

  @Test
  void switchingToTheContainersSectionAsksForItsWindow() {
    requests.showSettings(ListingWindow.EMPTY);
    sent.clear();

    requests.showSettings(new ListingWindow(0, 6));

    assertEquals(List.of("showing 7 0,6"), sent);
  }

  @Test
  void leavingTheSettingsTabTellsTheServerOnce() {
    requests.showSettings(new ListingWindow(0, 6));
    sent.clear();

    requests.hideSettings();
    requests.hideSettings();

    assertEquals(List.of("closed 7"), sent);
    assertFalse(requests.isSettingsShowing());
  }

  @Test
  void aPanelNobodyOpenedIsNeverClosed() {
    requests.hideSettings();

    assertEquals(List.of(), sent);
  }

  @Test
  void theWindowAlreadyAskedForIsNotAskedForAgain() {
    requests.showSettings(new ListingWindow(0, 6));

    assertFalse(requests.needsContainers(new ListingWindow(0, 6)));
    assertTrue(requests.needsContainers(new ListingWindow(6, 6)));
  }

  @Test
  void closingThePanelForgetsTheWindowItHeld() {
    requests.showSettings(new ListingWindow(0, 6));

    requests.hideSettings();

    assertTrue(requests.needsContainers(new ListingWindow(0, 6)));
  }

  @Test
  void theSameQueryAndWindowIsOnlySentOnce() {
    requests.rows(new ListingWindow(0, 40), "redstone", Set.of(), TagCombinationMode.DEFAULT);

    assertFalse(
        requests.needsRows(
            new ListingWindow(0, 40), "redstone", Set.of(), TagCombinationMode.DEFAULT));
    assertTrue(
        requests.needsRows(
            new ListingWindow(0, 40), "cobble", Set.of(), TagCombinationMode.DEFAULT));
  }
}
