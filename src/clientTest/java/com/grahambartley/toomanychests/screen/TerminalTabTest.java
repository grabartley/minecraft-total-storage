package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalTabTest {

  @ParameterizedTest
  @EnumSource(TerminalTab.class)
  void aClickAnywhereOnATabReachesIt(TerminalTab tab) {
    final int left = TerminalScreenLayout.TOOLBAR_X;
    final int top = TerminalScreenLayout.tabY(tab.index());
    final int size = TerminalScreenLayout.BUTTON_SIZE;

    assertEquals(tab, TerminalTab.at(left, top));
    assertEquals(tab, TerminalTab.at(left + size - 1, top + size - 1));
  }

  @Test
  void aClickBesideTheStripIsNoTab() {
    assertNull(TerminalTab.at(TerminalScreenLayout.TOOLBAR_X - 1, TerminalScreenLayout.TOOLBAR_Y));
    assertNull(
        TerminalTab.at(
            TerminalScreenLayout.TOOLBAR_X + TerminalScreenLayout.BUTTON_SIZE,
            TerminalScreenLayout.TOOLBAR_Y));
    assertNull(TerminalTab.at(TerminalScreenLayout.TOOLBAR_X, TerminalScreenLayout.TOOLBAR_Y - 1));
    assertNull(TerminalTab.at(TerminalScreenLayout.GRID_X, TerminalScreenLayout.GRID_Y));
  }

  /** The actions sit under the strip, so a click on one must not switch view. */
  @Test
  void aClickOnTheActionsBelowTheStripIsNoTab() {
    assertNull(TerminalTab.at(TerminalScreenLayout.TOOLBAR_X, TerminalScreenLayout.actionY(0)));
  }

  @Test
  void theTabsAreNumberedInTheOrderTheyAreDrawn() {
    assertEquals(0, TerminalTab.ITEMS.index());
    assertEquals(1, TerminalTab.SETTINGS.index());
  }

  @Test
  void onlyOneOfTheTwoViewsIsTheSettingsOne() {
    assertTrue(TerminalTab.SETTINGS.isSettings());
    assertFalse(TerminalTab.ITEMS.isSettings());
  }
}
