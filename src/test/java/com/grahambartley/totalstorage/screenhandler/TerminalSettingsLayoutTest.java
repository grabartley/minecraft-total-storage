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

class TerminalSettingsLayoutTest {
  private static final int LINE_HEIGHT = 9;

  static Stream<Integer> everySection() {
    return IntStream.range(0, TerminalSettingsLayout.SECTION_COUNT).boxed();
  }

  static Stream<Integer> everyListRow() {
    return IntStream.range(0, TerminalSettingsLayout.ROWS).boxed();
  }

  static Stream<Integer> everySetting() {
    return IntStream.range(0, TerminalSettingsLayout.SETTING_COUNT).boxed();
  }

  @Test
  void theFrameSpansTheFilterColumnAndTheListingAndEndsWithTheGrid() {
    assertEquals(TerminalScreenLayout.SIDE_X, TerminalSettingsLayout.FRAME_X);
    assertEquals(
        TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalSettingsLayout.FRAME_X + TerminalSettingsLayout.FRAME_WIDTH);
    assertEquals(
        TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT,
        TerminalSettingsLayout.FRAME_Y + TerminalSettingsLayout.FRAME_HEIGHT);
  }

  @Test
  void theFrameEndsAboveTheInventoryLabel() {
    assertTrue(
        TerminalSettingsLayout.FRAME_Y + TerminalSettingsLayout.FRAME_HEIGHT
            < TerminalScreenLayout.INVENTORY_LABEL_Y);
  }

  @ParameterizedTest
  @MethodSource("everySection")
  void everySectionTabIsClickableWhereItIsDrawn(int section) {
    final int left = TerminalSettingsLayout.sectionTabX(section);

    assertEquals(
        section, TerminalSettingsLayout.sectionTabAt(left, TerminalSettingsLayout.SECTION_TAB_Y));
    assertEquals(
        section,
        TerminalSettingsLayout.sectionTabAt(
            left + TerminalSettingsLayout.SECTION_TAB_WIDTH - 1,
            TerminalSettingsLayout.FRAME_Y - 1));
  }

  @Test
  void theSectionTabsSpanTheFrameWithoutOverlapping() {
    assertEquals(TerminalSettingsLayout.FRAME_X, TerminalSettingsLayout.sectionTabX(0));
    assertEquals(
        TerminalSettingsLayout.sectionTabX(0) + TerminalSettingsLayout.SECTION_TAB_WIDTH,
        TerminalSettingsLayout.sectionTabX(1));
    assertTrue(
        TerminalSettingsLayout.sectionTabX(TerminalSettingsLayout.SECTION_COUNT - 1)
                + TerminalSettingsLayout.SECTION_TAB_WIDTH
            <= TerminalSettingsLayout.FRAME_X + TerminalSettingsLayout.FRAME_WIDTH,
        "the strip runs past the frame");
  }

  @Test
  void theSelectedTabRisesAboveTheOthersAndJoinsTheFrame() {
    assertTrue(TerminalSettingsLayout.SELECTED_TAB_Y < TerminalSettingsLayout.SECTION_TAB_Y);
    assertTrue(
        TerminalSettingsLayout.SELECTED_TAB_Y + TerminalSettingsLayout.SELECTED_TAB_HEIGHT
            > TerminalSettingsLayout.FRAME_Y,
        "the selected tab must cover the frame's top edge to join it");
    assertTrue(
        TerminalSettingsLayout.SELECTED_TAB_Y >= TerminalScreenLayout.TITLE_Y + LINE_HEIGHT,
        "the selected tab runs into the title");
  }

  @Test
  void aClickInsideTheFrameOrBesideTheStripIsNoSectionTab() {
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.sectionTabAt(
            TerminalSettingsLayout.FRAME_X, TerminalSettingsLayout.FRAME_Y));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.sectionTabAt(
            TerminalSettingsLayout.FRAME_X - 1, TerminalSettingsLayout.SECTION_TAB_Y));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.sectionTabAt(
            TerminalSettingsLayout.FRAME_X, TerminalSettingsLayout.SELECTED_TAB_Y - 1));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, TerminalSettingsLayout.SECTION_COUNT, 100})
  void anIndexThatIsNotASectionIsRefusedRatherThanGuessedAt(int section) {
    assertThrows(
        IndexOutOfBoundsException.class, () -> TerminalSettingsLayout.sectionTabX(section));
  }

  @Test
  void theListSitsInsideTheFrameWithItsScrollbarBesideIt() {
    assertTrue(TerminalSettingsLayout.LIST_X > TerminalSettingsLayout.FRAME_X);
    assertTrue(TerminalSettingsLayout.LIST_Y > TerminalSettingsLayout.FRAME_Y);
    assertTrue(
        TerminalSettingsLayout.LIST_X + TerminalSettingsLayout.LIST_WIDTH
            < TerminalSettingsLayout.SCROLLBAR_X,
        "the rows run under the scrollbar");
    assertTrue(
        TerminalSettingsLayout.SCROLLBAR_X + TerminalSettingsLayout.SCROLLBAR_WIDTH
            < TerminalSettingsLayout.FRAME_X + TerminalSettingsLayout.FRAME_WIDTH,
        "the scrollbar runs under the frame");
    assertTrue(
        TerminalSettingsLayout.LIST_Y + TerminalSettingsLayout.LIST_HEIGHT
            < TerminalSettingsLayout.FRAME_Y + TerminalSettingsLayout.FRAME_HEIGHT,
        "the last row runs under the frame");
  }

  @Test
  void aRowHoldsAVanillaSlotAndAButton() {
    assertTrue(TerminalSettingsLayout.ROW_HEIGHT >= TerminalScreenLayout.SLOT_SIZE + 2);
    assertTrue(TerminalSettingsLayout.ROW_HEIGHT >= TerminalSettingsLayout.BUTTON_HEIGHT);
  }

  @ParameterizedTest
  @MethodSource("everyListRow")
  void everyListRowIsClickableAtItsOwnCorners(int row) {
    final int top = TerminalSettingsLayout.rowY(row);
    final int right = TerminalSettingsLayout.LIST_X + TerminalSettingsLayout.LIST_WIDTH - 1;

    assertEquals(row, TerminalSettingsLayout.rowAt(TerminalSettingsLayout.LIST_X, top));
    assertEquals(
        row, TerminalSettingsLayout.rowAt(right, top + TerminalSettingsLayout.ROW_HEIGHT - 1));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, TerminalSettingsLayout.ROWS, 100})
  void anIndexThatIsNotAListRowIsRefusedRatherThanGuessedAt(int row) {
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalSettingsLayout.rowY(row));
  }

  @Test
  void aClickAboveBelowOrBesideTheListIsOverNoRow() {
    final int bottom = TerminalSettingsLayout.LIST_Y + TerminalSettingsLayout.LIST_HEIGHT;

    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.rowAt(
            TerminalSettingsLayout.LIST_X, TerminalSettingsLayout.LIST_Y - 1));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.rowAt(TerminalSettingsLayout.LIST_X, bottom));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.rowAt(
            TerminalSettingsLayout.LIST_X - 1, TerminalSettingsLayout.LIST_Y));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.rowAt(
            TerminalSettingsLayout.SCROLLBAR_X, TerminalSettingsLayout.LIST_Y));
  }

  @ParameterizedTest
  @MethodSource("everySetting")
  void everySettingButtonSitsInsideTheFrame(int row) {
    final int top = TerminalSettingsLayout.settingY(row);

    assertTrue(top > TerminalSettingsLayout.FRAME_Y);
    assertTrue(
        top + TerminalSettingsLayout.BUTTON_HEIGHT
            < TerminalSettingsLayout.FRAME_Y + TerminalSettingsLayout.FRAME_HEIGHT);
    assertTrue(
        TerminalSettingsLayout.BUTTON_X + TerminalSettingsLayout.BUTTON_WIDTH
            <= TerminalSettingsLayout.CONTENT_RIGHT);
  }

  @Test
  void theSettingsAreEvenlySpacedAndDoNotTouch() {
    for (int row = 1; row < TerminalSettingsLayout.SETTING_COUNT; row++) {
      assertEquals(
          TerminalSettingsLayout.settingY(row - 1)
              + TerminalSettingsLayout.BUTTON_HEIGHT
              + TerminalSettingsLayout.SETTING_GAP,
          TerminalSettingsLayout.settingY(row));
    }
    assertTrue(TerminalSettingsLayout.SETTING_GAP > 0);
  }

  @Test
  void theSettingsAreCentredInTheFrame() {
    final int above = TerminalSettingsLayout.settingY(0) - TerminalSettingsLayout.FRAME_Y;
    final int below =
        TerminalSettingsLayout.FRAME_Y
            + TerminalSettingsLayout.FRAME_HEIGHT
            - (TerminalSettingsLayout.settingY(TerminalSettingsLayout.SETTING_COUNT - 1)
                + TerminalSettingsLayout.BUTTON_HEIGHT);

    assertTrue(Math.abs(above - below) <= 1, "above " + above + " below " + below);
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, TerminalSettingsLayout.SETTING_COUNT})
  void anIndexThatIsNotASettingIsRefusedRatherThanGuessedAt(int row) {
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalSettingsLayout.settingY(row));
  }

  @Test
  void aDescriptionStopsShortOfItsButton() {
    assertTrue(
        TerminalSettingsLayout.CONTENT_X + TerminalSettingsLayout.DESCRIPTION_WIDTH
            < TerminalSettingsLayout.BUTTON_X);
  }
}
