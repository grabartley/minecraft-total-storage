package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import org.junit.jupiter.api.Test;

class TerminalNoticeBannerTest {
  @Test
  void onTheSettingsTabTheStripStartsBelowTheSectionTabs() {
    assertTrue(
        TerminalSettingsLayout.CONTENT_Y
            >= TerminalSettingsLayout.PANEL_Y + TerminalSettingsLayout.SECTION_TAB_HEIGHT,
        "the strip would overlap the section tabs");
  }

  @Test
  void theSettingsStripSpansThePanelItSitsInRatherThanTheGridsWidth() {
    assertTrue(
        TerminalSettingsLayout.PANEL_WIDTH > TerminalScreenLayout.NOTICE_WIDTH,
        "the settings panel is meant to be wider than the item grid");
  }

  @Test
  void aNoticeRowIsAsTallAsASettingsRowSoItCoversWholeRows() {
    assertEquals(TerminalSettingsLayout.ROW_HEIGHT, TerminalScreenLayout.SLOT_SIZE);
  }
}
