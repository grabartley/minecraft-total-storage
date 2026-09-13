package com.grahambartley.toomanychests.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TagsSectionLayoutTest {
  @Test
  void theNewTagRowSitsAboveTheTagsItCreates() {
    assertTrue(TagsSectionLayout.NEW_TAG_ROW < TagsSectionLayout.FIRST_TAG_ROW);
  }

  @Test
  void everyRowBelowTheNewTagRowCanHoldATag() {
    assertEquals(
        TerminalSettingsLayout.ROWS - TagsSectionLayout.FIRST_TAG_ROW, TagsSectionLayout.TAG_ROWS);
    assertTrue(TagsSectionLayout.TAG_ROWS >= 3);
  }

  @Test
  void theDeleteControlStaysInsideTheRowItBelongsTo() {
    assertTrue(
        TagsSectionLayout.DELETE_X + TagsSectionLayout.CONTROL_SIZE
            <= TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.ROW_WIDTH);
    assertTrue(
        TagsSectionLayout.deleteY(TagsSectionLayout.FIRST_TAG_ROW) + TagsSectionLayout.CONTROL_SIZE
            <= TerminalSettingsLayout.rowY(TagsSectionLayout.FIRST_TAG_ROW)
                + TerminalSettingsLayout.ROW_HEIGHT);
  }

  @Test
  void theNameHasRoomBesideTheDeleteControl() {
    assertTrue(TagsSectionLayout.NAME_WIDTH > 0);
    assertTrue(
        TerminalSettingsLayout.PANEL_X + TagsSectionLayout.NAME_WIDTH
            <= TagsSectionLayout.DELETE_X);
  }

  @Test
  void aClickOnTheDeleteControlIsNotAClickOnTheName() {
    final int row = TagsSectionLayout.FIRST_TAG_ROW;
    assertEquals(
        TagRowRegion.DELETE,
        TagsSectionLayout.regionAt(
            TagsSectionLayout.DELETE_X + 1, TagsSectionLayout.deleteY(row) + 1));
    assertEquals(
        TagRowRegion.NAME,
        TagsSectionLayout.regionAt(
            TerminalSettingsLayout.PANEL_X + 1, TerminalSettingsLayout.rowY(row) + 1));
  }

  @Test
  void theCopyControlSitsOnTheNewTagRowSoNoTagRowIsSpentOnIt() {
    assertTrue(
        TagsSectionLayout.isOverCopy(
            TagsSectionLayout.DELETE_X + 1,
            TagsSectionLayout.deleteY(TagsSectionLayout.NEW_TAG_ROW) + 1));
  }

  @Test
  void aClickOnTheRestOfTheNewTagRowIsNotAClickOnTheCopyControl() {
    assertFalse(
        TagsSectionLayout.isOverCopy(
            TerminalSettingsLayout.PANEL_X + 1,
            TerminalSettingsLayout.rowY(TagsSectionLayout.NEW_TAG_ROW) + 1));
  }

  @Test
  void theCopyControlBelongsToTheNewTagRowAlone() {
    assertFalse(
        TagsSectionLayout.isOverCopy(
            TagsSectionLayout.DELETE_X + 1,
            TagsSectionLayout.deleteY(TagsSectionLayout.FIRST_TAG_ROW) + 1));
  }

  @Test
  void theNewTagRowIsNeverATagRow() {
    assertEquals(
        TagRowRegion.NONE,
        TagsSectionLayout.regionAt(
            TerminalSettingsLayout.PANEL_X + 1,
            TerminalSettingsLayout.rowY(TagsSectionLayout.NEW_TAG_ROW) + 1));
    assertTrue(
        TagsSectionLayout.isOverNewTag(
            TerminalSettingsLayout.PANEL_X + 1,
            TerminalSettingsLayout.rowY(TagsSectionLayout.NEW_TAG_ROW) + 1));
  }

  @Test
  void aPointOutsideTheRowsIsNeitherARegionNorTheNewTagRow() {
    assertEquals(
        TagRowRegion.NONE,
        TagsSectionLayout.regionAt(
            TerminalSettingsLayout.PANEL_X - 5, TerminalSettingsLayout.CONTENT_Y));
    assertFalse(
        TagsSectionLayout.isOverNewTag(
            TerminalSettingsLayout.PANEL_X - 5, TerminalSettingsLayout.CONTENT_Y));
  }

  @Test
  void theTagUnderAPointCountsFromWhereTheListWasScrolledTo() {
    final int row = TagsSectionLayout.FIRST_TAG_ROW;
    assertEquals(
        7,
        TagsSectionLayout.tagIndexAt(
            TerminalSettingsLayout.PANEL_X + 1, TerminalSettingsLayout.rowY(row) + 1, 7));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TagsSectionLayout.tagIndexAt(
            TerminalSettingsLayout.PANEL_X + 1,
            TerminalSettingsLayout.rowY(TagsSectionLayout.NEW_TAG_ROW) + 1,
            7));
  }
}
