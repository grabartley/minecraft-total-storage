package com.grahambartley.toomanychests.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;
import org.junit.jupiter.api.Test;

class TerminalNoticeBannerTest {
  @Test
  void onTheSettingsTabTheStripStartsBelowTheSectionTabs() {
    assertEquals(
        TerminalSettingsLayout.CONTENT_Y,
        TerminalScreenLayout.NOTICE_Y + TerminalNoticeBanner.settingsDrop());
  }

  @Test
  void theStripClearsTheSectionTabsEntirely() {
    assertTrue(
        TerminalNoticeBanner.settingsDrop() >= TerminalSettingsLayout.SECTION_TAB_HEIGHT,
        "The strip would overlap the section tabs by "
            + (TerminalSettingsLayout.SECTION_TAB_HEIGHT - TerminalNoticeBanner.settingsDrop()));
  }

  @Test
  void aNoticeRowIsAsTallAsASettingsRowSoItCoversWholeRows() {
    assertEquals(TerminalSettingsLayout.ROW_HEIGHT, TerminalScreenLayout.SLOT_SIZE);
  }
}
