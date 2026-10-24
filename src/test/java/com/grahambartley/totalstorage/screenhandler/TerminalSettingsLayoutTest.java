package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalSettingsLayoutTest {

  @Test
  void theSettingsPanelFillsEveryInchTheItemsTabUsesIncludingTheFilterColumn() {
    assertEquals(TerminalScreenLayout.FILTER_X, TerminalSettingsLayout.INSET_X);
    assertEquals(TerminalScreenLayout.SEARCH_Y, TerminalSettingsLayout.INSET_Y);
    assertEquals(
        TerminalScreenLayout.INSET_X + TerminalScreenLayout.INSET_WIDTH,
        TerminalSettingsLayout.INSET_X + TerminalSettingsLayout.INSET_WIDTH);
    assertEquals(
        TerminalScreenLayout.INSET_Y + TerminalScreenLayout.INSET_HEIGHT,
        TerminalSettingsLayout.INSET_Y + TerminalSettingsLayout.INSET_HEIGHT);
  }

  @Test
  void thePanelSitsInsideItsRecessByTheBorderTheChromeDraws() {
    assertEquals(
        TerminalSettingsLayout.INSET_X + TerminalScreenLayout.INSET_BORDER,
        TerminalSettingsLayout.PANEL_X);
    assertEquals(
        TerminalSettingsLayout.INSET_Y + TerminalScreenLayout.INSET_BORDER,
        TerminalSettingsLayout.PANEL_Y);
    assertEquals(
        TerminalSettingsLayout.INSET_WIDTH - 2 * TerminalScreenLayout.INSET_BORDER,
        TerminalSettingsLayout.PANEL_WIDTH);
    assertEquals(
        TerminalSettingsLayout.INSET_HEIGHT - 2 * TerminalScreenLayout.INSET_BORDER,
        TerminalSettingsLayout.PANEL_HEIGHT);
  }

  @Test
  void theWiderPanelBuysRoomTheOldGridSizedOneDidNotHave() {
    assertTrue(
        TerminalSettingsLayout.PANEL_WIDTH > TerminalScreenLayout.GRID_WIDTH,
        "the settings tabs are meant to use the filter column's width too");
    assertTrue(
        TerminalSettingsLayout.ROWS >= 5,
        "a seven container network should be close to fitting, got "
            + TerminalSettingsLayout.ROWS
            + " rows");
  }

  @Test
  void theScrollbarSitsAtTheRightEdgeOfThePanelAndTheRowsStopShortOfIt() {
    assertEquals(
        TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.PANEL_WIDTH,
        TerminalSettingsLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH);
    assertTrue(
        TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.ROW_WIDTH
            <= TerminalSettingsLayout.SCROLLBAR_X,
        "a container row would run under the scrollbar");
  }

  static Stream<Integer> everySection() {
    return IntStream.range(0, TerminalSettingsLayout.SECTION_COUNT).boxed();
  }

  static Stream<Integer> everySettingsRow() {
    return IntStream.range(0, TerminalSettingsLayout.ROWS).boxed();
  }

  @ParameterizedTest
  @MethodSource("everySection")
  void everySectionTabIsClickableWhereItIsDrawn(int section) {
    int left = TerminalSettingsLayout.sectionTabX(section);

    assertEquals(
        section, TerminalSettingsLayout.sectionTabAt(left, TerminalSettingsLayout.PANEL_Y));
    assertEquals(
        section,
        TerminalSettingsLayout.sectionTabAt(
            left + TerminalSettingsLayout.SECTION_TAB_WIDTH - 1,
            TerminalSettingsLayout.PANEL_Y + TerminalSettingsLayout.SECTION_TAB_HEIGHT - 1));
  }

  @Test
  void theSectionTabsSpanThePanelWithoutOverlapping() {
    assertEquals(TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.sectionTabX(0));
    assertEquals(
        TerminalSettingsLayout.sectionTabX(0) + TerminalSettingsLayout.SECTION_TAB_WIDTH,
        TerminalSettingsLayout.sectionTabX(1));
    assertTrue(
        TerminalSettingsLayout.sectionTabX(TerminalSettingsLayout.SECTION_COUNT - 1)
                + TerminalSettingsLayout.SECTION_TAB_WIDTH
            <= TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.PANEL_WIDTH,
        "the strip runs past the panel");
  }

  @Test
  void aClickBelowTheStripIsNoSectionTab() {
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.sectionTabAt(
            TerminalSettingsLayout.PANEL_X,
            TerminalSettingsLayout.PANEL_Y + TerminalSettingsLayout.SECTION_TAB_HEIGHT));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.sectionTabAt(
            TerminalSettingsLayout.PANEL_X - 1, TerminalSettingsLayout.PANEL_Y));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, TerminalSettingsLayout.SECTION_COUNT, 100})
  void anIndexThatIsNotASectionIsRefusedRatherThanGuessedAt(int section) {
    assertThrows(
        IndexOutOfBoundsException.class, () -> TerminalSettingsLayout.sectionTabX(section));
  }

  @Test
  void thereIsRoomForEnoughRowsToBeAList() {
    assertTrue(
        TerminalSettingsLayout.ROWS >= 4,
        "a list of " + TerminalSettingsLayout.ROWS + " rows is not navigable");
  }

  @Test
  void aRowIsTallEnoughToHoldAnIconAndAControl() {
    assertTrue(
        TerminalSettingsLayout.ROW_HEIGHT >= TerminalSettingsLayout.CONTROL_SIZE + 2,
        "a row of "
            + TerminalSettingsLayout.ROW_HEIGHT
            + " cannot hold a control of "
            + TerminalSettingsLayout.CONTROL_SIZE);
    assertEquals(
        TerminalSettingsLayout.ROW_HEIGHT,
        2 * TerminalSettingsLayout.CONTROL_INSET + TerminalSettingsLayout.CONTROL_SIZE);
  }

  @Test
  void theRowsStartBelowTheStripAndEndInsideTheRecess() {
    assertTrue(
        TerminalSettingsLayout.CONTENT_Y
            >= TerminalSettingsLayout.PANEL_Y + TerminalSettingsLayout.SECTION_TAB_HEIGHT,
        "the rows run under the strip");
    assertTrue(
        TerminalSettingsLayout.rowY(TerminalSettingsLayout.ROWS - 1)
                + TerminalSettingsLayout.ROW_HEIGHT
            <= TerminalSettingsLayout.PANEL_Y + TerminalSettingsLayout.PANEL_HEIGHT,
        "the last row runs past the recess");
  }

  @ParameterizedTest
  @MethodSource("everySettingsRow")
  void everySettingsRowIsClickableWhereItIsDrawn(int row) {
    assertEquals(
        row,
        TerminalSettingsLayout.rowAt(
            TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.rowY(row)));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 100})
  void anIndexThatIsNotASettingsRowIsRefusedRatherThanGuessedAt(int row) {
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalSettingsLayout.rowY(row));
  }

  @Test
  void aClickOnTheStripOrBesideTheRowsIsOverNoRow() {
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.rowAt(
            TerminalSettingsLayout.PANEL_X, TerminalSettingsLayout.CONTENT_Y - 1));
    assertEquals(
        TerminalSettingsLayout.NO_ROW,
        TerminalSettingsLayout.rowAt(
            TerminalSettingsLayout.PANEL_X - 1, TerminalSettingsLayout.CONTENT_Y));
  }

  @Test
  void aSettingsRowSpansThePanelAndTheGutterIsStillTheScrollbars() {
    assertEquals(TerminalSettingsLayout.PANEL_WIDTH, TerminalSettingsLayout.FULL_ROW_WIDTH);
    assertEquals(
        0,
        TerminalSettingsLayout.rowAt(
            TerminalScreenLayout.SCROLLBAR_X, TerminalSettingsLayout.CONTENT_Y));
    assertTrue(
        TerminalSettingsLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X, TerminalSettingsLayout.CONTENT_Y));
  }

  @Test
  void aContainerRowLeavesTheGutterToItsScrollbar() {
    assertTrue(
        TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.ROW_WIDTH
            <= TerminalScreenLayout.SCROLLBAR_X,
        "the container rows run under the scrollbar");
  }

  @Test
  void aPointBesideTheSettingsScrollbarIsNotOverIt() {
    assertFalse(
        TerminalSettingsLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X - 1, TerminalSettingsLayout.CONTENT_Y));
    assertFalse(
        TerminalSettingsLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X, TerminalSettingsLayout.CONTENT_Y - 1));
    assertFalse(
        TerminalSettingsLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X,
            TerminalSettingsLayout.CONTENT_Y + TerminalSettingsLayout.CONTENT_HEIGHT));
  }
}
