package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class SettingsSectionTest {
  @ParameterizedTest
  @EnumSource(SettingsSection.class)
  void aClickAnywhereOnASectionTabReachesIt(SettingsSection section) {
    int left = TerminalSettingsLayout.sectionTabX(section.index());

    assertEquals(section, SettingsSection.at(left, TerminalSettingsLayout.PANEL_Y));
    assertEquals(
        section,
        SettingsSection.at(
            left + TerminalSettingsLayout.SECTION_TAB_WIDTH - 1,
            TerminalSettingsLayout.PANEL_Y + TerminalSettingsLayout.SECTION_TAB_HEIGHT - 1));
  }

  @Test
  void aClickBelowTheStripIsNoSection() {
    assertNull(
        SettingsSection.at(
            TerminalSettingsLayout.PANEL_X,
            TerminalSettingsLayout.PANEL_Y + TerminalSettingsLayout.SECTION_TAB_HEIGHT));
    assertNull(
        SettingsSection.at(TerminalSettingsLayout.PANEL_X - 1, TerminalSettingsLayout.PANEL_Y));
  }

  @Test
  void theSectionsAreNumberedInTheOrderTheyAreDrawn() {
    assertEquals(0, SettingsSection.TERMINAL.index());
    assertEquals(1, SettingsSection.CONTAINERS.index());
    assertEquals(2, SettingsSection.TAGS.index());
  }

  @ParameterizedTest
  @EnumSource(SettingsSection.class)
  void everySectionIsNamedUnderItsOwnKey(SettingsSection section) {
    assertEquals("section." + section.name().toLowerCase(java.util.Locale.ROOT), section.label());
  }

  @Test
  void noTwoSectionsShareAName() {
    final Set<String> labels = new HashSet<>();
    for (SettingsSection section : SettingsSection.values()) {
      assertTrue(labels.add(section.label()), section + " reuses another section's name");
    }
  }

  @Test
  void everySectionOnTheStripHasATabToReachItBy() {
    assertEquals(TerminalSettingsLayout.SECTION_COUNT, SettingsSection.values().length);
  }
}
