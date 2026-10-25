package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FilterColumnLayoutTest {
  @Test
  void theColumnSitsInsideThePanelWithoutOverlappingTheGrid() {
    assertTrue(FilterColumnLayout.X >= TerminalScreenLayout.EDGE);
    assertTrue(
        FilterColumnLayout.X + FilterColumnLayout.WIDTH < TerminalScreenLayout.GRID_X,
        "the column runs into the grid");
  }

  @Test
  void theColumnEndsWhereTheGridEnds() {
    assertEquals(
        TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT,
        FilterColumnLayout.Y + FilterColumnLayout.HEIGHT);
    assertEquals(TerminalScreenLayout.SEARCH_Y, FilterColumnLayout.Y);
  }

  @Test
  void thereIsRoomForEnoughRowsToBeAList() {
    assertTrue(
        FilterColumnLayout.ROWS >= 4,
        "a column of " + FilterColumnLayout.ROWS + " rows is not navigable");
  }

  @Test
  void theRowsStayInsideTheColumn() {
    assertTrue(
        FilterColumnLayout.rowY(FilterColumnLayout.ROWS - 1) + FilterColumnLayout.ROW_HEIGHT
            <= FilterColumnLayout.CONTENT_Y + FilterColumnLayout.CONTENT_HEIGHT);
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 100})
  void anIndexThatIsNotARowIsRefusedRatherThanGuessedAt(int row) {
    assertThrows(IndexOutOfBoundsException.class, () -> FilterColumnLayout.rowY(row));
  }

  @Test
  void aPointInsideARowResolvesToThatRow() {
    for (int row = 0; row < FilterColumnLayout.ROWS; row++) {
      assertEquals(
          row, FilterColumnLayout.rowAt(FilterColumnLayout.X + 1, FilterColumnLayout.rowY(row)));
    }
  }

  @Test
  void aPointOutsideTheRowsIsNotARow() {
    assertEquals(
        FilterColumnLayout.NO_ROW,
        FilterColumnLayout.rowAt(FilterColumnLayout.X - 1, FilterColumnLayout.CONTENT_Y));
    assertEquals(
        FilterColumnLayout.NO_ROW,
        FilterColumnLayout.rowAt(FilterColumnLayout.X + 1, FilterColumnLayout.CONTENT_Y - 1));
    assertEquals(
        FilterColumnLayout.NO_ROW,
        FilterColumnLayout.rowAt(
            FilterColumnLayout.X + 1,
            FilterColumnLayout.CONTENT_Y + FilterColumnLayout.CONTENT_HEIGHT + 1));
  }

  @Test
  void aNameAndItsCountNeverMeet() {
    final int countWidth = 12;

    assertTrue(
        FilterColumnLayout.nameLeft(true) + FilterColumnLayout.nameWidth(countWidth, true)
            < FilterColumnLayout.countLeft(countWidth));
    assertTrue(
        FilterColumnLayout.nameLeft(false) + FilterColumnLayout.nameWidth(countWidth, false)
            < FilterColumnLayout.countLeft(countWidth));
  }

  @Test
  void aCheckMarkPushesTheNameRightByItsOwnWidthAndGap() {
    assertEquals(
        FilterColumnLayout.nameLeft(false)
            + FilterColumnLayout.CHECK_SIZE
            + FilterColumnLayout.CHECK_GAP,
        FilterColumnLayout.nameLeft(true));
  }

  @Test
  void aCountTooWideForTheRowLeavesNoRoomRatherThanANegativeWidth() {
    assertEquals(0, FilterColumnLayout.nameWidth(FilterColumnLayout.ROW_WIDTH, true));
  }

  @Test
  void theHeaderControlsDoNotOverlapEachOther() {
    assertTrue(FilterColumnLayout.X + FilterColumnLayout.MODE_WIDTH <= FilterColumnLayout.CLEAR_X);
    assertTrue(
        FilterColumnLayout.CLEAR_X + FilterColumnLayout.CONTROL_SIZE
            <= FilterColumnLayout.X + FilterColumnLayout.WIDTH);
  }

  @Test
  void theScrollbarSitsInsideTheColumnBesideTheRows() {
    assertTrue(
        FilterColumnLayout.ROW_WIDTH <= FilterColumnLayout.SCROLLBAR_X - FilterColumnLayout.X);
    assertTrue(
        FilterColumnLayout.SCROLLBAR_X + FilterColumnLayout.SCROLLBAR_WIDTH
            <= FilterColumnLayout.X + FilterColumnLayout.WIDTH);
    assertTrue(
        FilterColumnLayout.isOverScrollbar(
            FilterColumnLayout.SCROLLBAR_X, FilterColumnLayout.CONTENT_Y));
  }

  @Test
  void aPointOverTheScrollbarIsNotAlsoARow() {
    assertEquals(
        FilterColumnLayout.NO_ROW,
        FilterColumnLayout.rowAt(FilterColumnLayout.SCROLLBAR_X, FilterColumnLayout.CONTENT_Y));
  }

  @Test
  void theColumnRegionCoversItsHeaderAndItsRows() {
    assertTrue(FilterColumnLayout.isOverColumn(FilterColumnLayout.X, FilterColumnLayout.Y));
    assertTrue(
        FilterColumnLayout.isOverColumn(FilterColumnLayout.X + 1, FilterColumnLayout.CONTENT_Y));
    assertFalse(FilterColumnLayout.isOverColumn(FilterColumnLayout.X - 1, FilterColumnLayout.Y));
    assertFalse(
        FilterColumnLayout.isOverColumn(
            FilterColumnLayout.X, FilterColumnLayout.Y + FilterColumnLayout.HEIGHT));
  }
}
