package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.listing.ListingWindow;
import org.junit.jupiter.api.Test;

class TerminalSettingsStateTest {
  @Test
  void aPanelNobodyHasOpenedIsNotPushedTo() {
    assertFalse(new TerminalSettingsState().isOpen());
  }

  @Test
  void showingThePanelIsWhatOpensIt() {
    TerminalSettingsState state = new TerminalSettingsState();

    state.showing(new ListingWindow(4, 8));

    assertTrue(state.isOpen());
    assertEquals(4, state.window().offset());
    assertEquals(8, state.window().length());
  }

  @Test
  void closingThePanelStopsItBeingSentAnything() {
    TerminalSettingsState state = new TerminalSettingsState();
    state.showing(new ListingWindow(4, 8));

    state.closed();

    assertFalse(state.isOpen());
  }

  @Test
  void aSectionWantingNoRowsStillCountsAsOpen() {
    TerminalSettingsState state = new TerminalSettingsState();

    state.showing(null);

    assertTrue(state.isOpen());
    assertEquals(ListingWindow.EMPTY, state.window());
  }
}
