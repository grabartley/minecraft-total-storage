package com.grahambartley.toomanychests.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.listing.ListingWindow;
import org.junit.jupiter.api.Test;

class TerminalSettingsStateTest {
  @Test
  void aPanelNobodyHasOpenedIsNotPushedTo() {
    assertFalse(new TerminalSettingsState().isOpen());
  }

  @Test
  void askingForRowsIsWhatOpensThePanel() {
    TerminalSettingsState state = new TerminalSettingsState();

    state.requested(new ListingWindow(4, 8));

    assertTrue(state.isOpen());
    assertEquals(4, state.window().offset());
    assertEquals(8, state.window().length());
  }

  @Test
  void closingThePanelStopsItBeingSentAnything() {
    TerminalSettingsState state = new TerminalSettingsState();
    state.requested(new ListingWindow(4, 8));

    state.closed();

    assertFalse(state.isOpen());
  }

  @Test
  void askingForNothingLeavesThePanelClosedRatherThanEmpty() {
    TerminalSettingsState state = new TerminalSettingsState();

    state.requested(null);

    assertFalse(state.isOpen());
    assertEquals(ListingWindow.EMPTY, state.window());
  }
}
