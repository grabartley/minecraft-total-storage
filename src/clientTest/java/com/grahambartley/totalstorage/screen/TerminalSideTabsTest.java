package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalSideTabsTest {
  @ParameterizedTest
  @EnumSource(TerminalTab.class)
  void aClickAnywhereOnATabReachesIt(TerminalTab tab) {
    final int top = TerminalSideTabs.top(tab);

    assertEquals(tab, TerminalSideTabs.at(TerminalSideTabs.X, top));
    assertEquals(
        tab,
        TerminalSideTabs.at(
            TerminalSideTabs.X + TerminalSideTabs.WIDTH - 1, top + TerminalSideTabs.HEIGHT - 1));
  }

  @Test
  void aClickBesideOrBetweenTheTabsIsNoTab() {
    final int top = TerminalSideTabs.top(TerminalTab.ITEMS);

    assertNull(TerminalSideTabs.at(TerminalSideTabs.X + TerminalSideTabs.WIDTH, top));
    assertNull(TerminalSideTabs.at(TerminalSideTabs.SELECTED_X - 1, top));
    assertNull(TerminalSideTabs.at(TerminalSideTabs.X, top - 2));
    assertNull(TerminalSideTabs.at(TerminalSideTabs.X, top + TerminalSideTabs.HEIGHT + 2));
  }

  @Test
  void theTabsHangOffTheRightEdgeOfTheWindowWithAGapBetweenThem() {
    assertEquals(TerminalScreenLayout.WINDOW_WIDTH, TerminalSideTabs.X);
    assertTrue(TerminalSideTabs.PITCH > TerminalSideTabs.HEIGHT);
    assertTrue(
        TerminalSideTabs.top(TerminalTab.SETTINGS) + TerminalSideTabs.HEIGHT
            < TerminalScreenLayout.WINDOW_HEIGHT);
  }

  @Test
  void theSelectedTabReachesBackIntoThePanelToJoinIt() {
    assertEquals(TerminalSideTabs.X - TerminalSideTabs.OVERLAP, TerminalSideTabs.SELECTED_X);
    assertEquals(
        TerminalSideTabs.X + TerminalSideTabs.WIDTH,
        TerminalSideTabs.SELECTED_X + TerminalSideTabs.SELECTED_WIDTH);
  }

  @ParameterizedTest
  @EnumSource(TerminalTab.class)
  void everyIconIsCentredInsideItsTab(TerminalTab tab) {
    final int top = TerminalSideTabs.top(tab);

    assertTrue(TerminalSideTabs.ICON_X > TerminalSideTabs.X);
    assertTrue(
        TerminalSideTabs.ICON_X + TerminalSideTabs.ICON_SIZE
            < TerminalSideTabs.X + TerminalSideTabs.WIDTH);
    assertEquals(
        top + TerminalSideTabs.HEIGHT - TerminalSideTabs.iconY(tab) - TerminalSideTabs.ICON_SIZE,
        TerminalSideTabs.iconY(tab) - top);
  }
}
