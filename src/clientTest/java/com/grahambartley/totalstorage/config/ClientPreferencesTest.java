package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClientPreferencesTest {
  @Test
  void aFreshInstallShowsBothIndicatorsWhereVanillaLeavesRoom() {
    final ClientPreferences defaults = ClientPreferences.defaults();

    assertTrue(defaults.sortButtonVisible());
    assertTrue(defaults.lockedSlotTintVisible());
    assertEquals(ScreenButtonAnchor.INSIDE_TOP_RIGHT, defaults.screenButtonAnchor());
  }

  @Test
  void anAnchorNobodyChosenFallsBackToTheDefault() {
    assertEquals(
        ClientPreferences.DEFAULT_SCREEN_BUTTON_ANCHOR,
        new ClientPreferences(true, null, true).screenButtonAnchor());
  }
}
