package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TagsSectionLayoutTest {
  private static final int NAME_X = TerminalSettingsLayout.LIST_X + 1;

  static Stream<Integer> everyRow() {
    return IntStream.range(0, TagsSectionLayout.TAG_ROWS).boxed();
  }

  @Test
  void theTagListAndItsFooterFitInsideTheFrame() {
    assertTrue(
        TerminalSettingsLayout.LIST_Y + TagsSectionLayout.LIST_HEIGHT <= TagsSectionLayout.FOOTER_Y,
        "the footer overlaps the last tag row");
    assertTrue(
        TagsSectionLayout.FOOTER_Y + 9
            < TerminalSettingsLayout.FRAME_Y + TerminalSettingsLayout.FRAME_HEIGHT,
        "the footer runs under the frame");
  }

  @ParameterizedTest
  @MethodSource("everyRow")
  void theControlsStayInsideTheRowTheyBelongTo(int row) {
    final int top = TagsSectionLayout.rowY(row);

    assertTrue(TagsSectionLayout.controlY(row) >= top);
    assertTrue(
        TagsSectionLayout.controlY(row) + TagsSectionLayout.CONTROL_SIZE
            <= top + TagsSectionLayout.ROW_HEIGHT);
    assertTrue(TagsSectionLayout.armedY(row) >= top);
    assertTrue(
        TagsSectionLayout.armedY(row) + TagsSectionLayout.ARMED_HEIGHT
            <= top + TagsSectionLayout.ROW_HEIGHT);
    assertTrue(
        TagsSectionLayout.DELETE_X + TagsSectionLayout.CONTROL_SIZE
            <= TerminalSettingsLayout.LIST_X + TerminalSettingsLayout.LIST_WIDTH);
  }

  @Test
  void theRenameAndDeleteControlsSitSideBySideWithAGap() {
    assertEquals(
        TagsSectionLayout.RENAME_X + TagsSectionLayout.CONTROL_SIZE + TagsSectionLayout.CONTROL_GAP,
        TagsSectionLayout.DELETE_X);
    assertTrue(TagsSectionLayout.CONTROL_GAP > 0);
  }

  @Test
  void theArmedConfirmEndsWhereTheDeleteControlEnds() {
    assertEquals(
        TagsSectionLayout.DELETE_X + TagsSectionLayout.CONTROL_SIZE,
        TagsSectionLayout.ARMED_X + TagsSectionLayout.ARMED_WIDTH);
  }

  @ParameterizedTest
  @MethodSource("everyRow")
  void aClickLandsOnTheControlItIsOver(int row) {
    final int controlY = TagsSectionLayout.controlY(row) + 1;

    assertEquals(
        TagRowRegion.DELETE,
        TagsSectionLayout.regionAt(TagsSectionLayout.DELETE_X + 1, controlY, false));
    assertEquals(
        TagRowRegion.RENAME,
        TagsSectionLayout.regionAt(TagsSectionLayout.RENAME_X + 1, controlY, false));
    assertEquals(
        TagRowRegion.NAME,
        TagsSectionLayout.regionAt(NAME_X, TagsSectionLayout.rowY(row) + 1, false));
  }

  @Test
  void anArmedRowOnlyDeletesFromItsConfirmButton() {
    final int row = 0;
    final int armedY = TagsSectionLayout.armedY(row) + 1;

    assertEquals(
        TagRowRegion.DELETE,
        TagsSectionLayout.regionAt(TagsSectionLayout.ARMED_X + 1, armedY, true));
    assertEquals(
        TagRowRegion.NAME, TagsSectionLayout.regionAt(TagsSectionLayout.ARMED_X - 1, armedY, true));
  }

  @Test
  void aPointBesideOrBelowTheRowsIsNoRegion() {
    assertEquals(
        TagRowRegion.NONE,
        TagsSectionLayout.regionAt(
            TerminalSettingsLayout.LIST_X - 1, TerminalSettingsLayout.LIST_Y, false));
    assertEquals(
        TagRowRegion.NONE,
        TagsSectionLayout.regionAt(
            NAME_X, TerminalSettingsLayout.LIST_Y + TagsSectionLayout.LIST_HEIGHT, false));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, TagsSectionLayout.TAG_ROWS})
  void anIndexThatIsNotARowIsRefusedRatherThanGuessedAt(int row) {
    assertThrows(IndexOutOfBoundsException.class, () -> TagsSectionLayout.rowY(row));
  }

  @Test
  void theTagUnderAPointCountsFromWhereTheListWasScrolledTo() {
    assertEquals(9, TagsSectionLayout.tagIndexAt(NAME_X, TagsSectionLayout.rowY(2) + 1, 7));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TagsSectionLayout.tagIndexAt(NAME_X, TerminalSettingsLayout.LIST_Y - 1, 7));
  }
}
