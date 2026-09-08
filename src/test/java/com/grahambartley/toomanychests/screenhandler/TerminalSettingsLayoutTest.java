package com.grahambartley.toomanychests.screenhandler;

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

  /**
   * The settings panel and the grid share one recess, so it must cover exactly what the grid does.
   */
  @Test
  void theSettingsPanelFillsTheRecessTheGridUses() {
    assertEquals(TerminalScreenLayout.GRID_X, TerminalSettingsLayout.PANEL_X);
    assertEquals(TerminalScreenLayout.GRID_Y, TerminalSettingsLayout.PANEL_Y);
    assertEquals(TerminalScreenLayout.GRID_HEIGHT, TerminalSettingsLayout.PANEL_HEIGHT);
    assertEquals(
        TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.PANEL_WIDTH);
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
  @ValueSource(ints = {-1, 2, 100})
  void anIndexThatIsNotASectionIsRefusedRatherThanGuessedAt(int section) {
    assertThrows(
        IndexOutOfBoundsException.class, () -> TerminalSettingsLayout.sectionTabX(section));
  }

  /** Enough rows to be a list rather than a peephole, which is what splitting the panel bought. */
  @Test
  void thereIsRoomForEnoughRowsToBeAList() {
    assertTrue(
        TerminalSettingsLayout.ROWS >= 4,
        "a list of " + TerminalSettingsLayout.ROWS + " rows is not navigable");
  }

  /** A row has to hold a sixteen pixel item icon and a control, with air around them. */
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

  /**
   * A row spans the whole panel, because only the section that scrolls has a gutter. Which section
   * that is belongs to the panel, so rejecting the gutter is proven in {@code
   * TerminalSettingsPanelTest} rather than here.
   */
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
