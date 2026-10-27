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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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
          });

  @Test
  void aSectionThatWantsNoRowsStillTellsTheServerThePanelIsShowing() {
    requests.showSettings(ListingWindow.EMPTY);

    assertEquals(List.of("showing 7 0,0"), sent);
  }

  @Test
  void switchingToTheContainersSectionAsksForItsWindow() {
    requests.showSettings(ListingWindow.EMPTY);
    sent.clear();

    requests.showSettings(new ListingWindow(0, 6));

    assertEquals(List.of("showing 7 0,6"), sent);
  }

  @ParameterizedTest(name = "asked for {0}+{1}, needs {2}+{3}: new request {4}")
  @CsvSource({
    "0, 34, 0, 6, false",
    "0, 34, 0, 34, false",
    "0, 34, 20, 14, false",
    "0, 34, 20, 19, true",
    "10, 30, 5, 10, true",
    "0, 34, 0, 0, true",
    "0, 0, 0, 0, false",
    "0, 0, 0, 6, true"
  })
  void onlyContainersOutsideWhatWasAskedForAreAskedForAgain(
      int askedOffset, int askedLength, int neededOffset, int neededLength, boolean expected) {
    requests.showSettings(new ListingWindow(askedOffset, askedLength));

    assertEquals(expected, requests.needsContainers(new ListingWindow(neededOffset, neededLength)));
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
