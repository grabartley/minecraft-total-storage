package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

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

  @Test
  void hidingTheSortButtonLeavesEverythingElseAlone() {
    final ClientPreferences hidden = ClientPreferences.defaults().withSortButtonVisible(false);

    assertEquals(false, hidden.sortButtonVisible());
    assertEquals(ClientPreferences.defaults().screenButtonAnchor(), hidden.screenButtonAnchor());
    assertEquals(
        ClientPreferences.defaults().lockedSlotTintVisible(), hidden.lockedSlotTintVisible());
  }

  @Test
  void turningOffTheTintLeavesEverythingElseAlone() {
    final ClientPreferences plain = ClientPreferences.defaults().withLockedSlotTintVisible(false);

    assertEquals(false, plain.lockedSlotTintVisible());
    assertEquals(ClientPreferences.defaults().sortButtonVisible(), plain.sortButtonVisible());
    assertEquals(ClientPreferences.defaults().screenButtonAnchor(), plain.screenButtonAnchor());
  }

  @ParameterizedTest
  @EnumSource(ScreenButtonAnchor.class)
  void movingTheButtonsLeavesEverythingElseAlone(ScreenButtonAnchor anchor) {
    final ClientPreferences moved = ClientPreferences.defaults().withScreenButtonAnchor(anchor);

    assertEquals(anchor, moved.screenButtonAnchor());
    assertEquals(ClientPreferences.defaults().sortButtonVisible(), moved.sortButtonVisible());
    assertEquals(
        ClientPreferences.defaults().lockedSlotTintVisible(), moved.lockedSlotTintVisible());
  }
}
