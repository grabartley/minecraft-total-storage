package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SettingsSectionTest {

  @ParameterizedTest
  @EnumSource(SettingsSection.class)
  void aClickAnywhereOnASectionTabReachesIt(SettingsSection section) {
    int left = TerminalScreenLayout.sectionTabX(section.index());

    assertEquals(section, SettingsSection.at(left, TerminalScreenLayout.SETTINGS_Y));
    assertEquals(
        section,
        SettingsSection.at(
            left + TerminalScreenLayout.SECTION_TAB_WIDTH - 1,
            TerminalScreenLayout.SETTINGS_Y + TerminalScreenLayout.SECTION_TAB_HEIGHT - 1));
  }

  @Test
  void aClickBelowTheStripIsNoSection() {
    assertNull(
        SettingsSection.at(
            TerminalScreenLayout.SETTINGS_X,
            TerminalScreenLayout.SETTINGS_Y + TerminalScreenLayout.SECTION_TAB_HEIGHT));
    assertNull(
        SettingsSection.at(TerminalScreenLayout.SETTINGS_X - 1, TerminalScreenLayout.SETTINGS_Y));
  }

  @Test
  void theSectionsAreNumberedInTheOrderTheyAreDrawn() {
    assertEquals(0, SettingsSection.TERMINAL.index());
    assertEquals(1, SettingsSection.CONTAINERS.index());
  }

  @ParameterizedTest
  @EnumSource(SettingsSection.class)
  void everySectionIsNamedUnderItsOwnKey(SettingsSection section) {
    assertEquals("section." + section.name().toLowerCase(java.util.Locale.ROOT), section.label());
  }

  @Test
  void noTwoSectionsShareAName() {
    assertFalse(SettingsSection.TERMINAL.label().equals(SettingsSection.CONTAINERS.label()));
  }
}
