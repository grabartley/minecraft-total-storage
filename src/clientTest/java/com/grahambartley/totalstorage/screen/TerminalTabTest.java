package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TerminalTabTest {
  @Test
  void theTabsAreNumberedInTheOrderTheyAreDrawn() {
    assertEquals(0, TerminalTab.ITEMS.index());
    assertEquals(1, TerminalTab.SETTINGS.index());
  }

  @ParameterizedTest
  @EnumSource(TerminalTab.class)
  void everyTabIsNamedUnderItsOwnKey(TerminalTab tab) {
    assertEquals(tab.name().toLowerCase(java.util.Locale.ROOT), tab.label());
  }

  @Test
  void noTwoTabsShareANameOrAnIcon() {
    assertNotEquals(TerminalTab.ITEMS.label(), TerminalTab.SETTINGS.label());
    assertNotEquals(TerminalTab.ITEMS.icon(), TerminalTab.SETTINGS.icon());
  }

  @Test
  void onlyOneOfTheTwoViewsIsTheSettingsOne() {
    assertTrue(TerminalTab.SETTINGS.isSettings());
    assertFalse(TerminalTab.ITEMS.isSettings());
  }
}
