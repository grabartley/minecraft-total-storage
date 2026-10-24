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
    assertTrue(FilterColumnLayout.X >= TerminalScreenLayout.PANEL_BORDER);
    assertTrue(
        FilterColumnLayout.X + FilterColumnLayout.WIDTH <= TerminalScreenLayout.INSET_X,
        "the column runs into the grid inset");
  }

  @Test
  void theColumnEndsWhereTheGridEnds() {
    assertEquals(
        TerminalScreenLayout.INSET_Y + TerminalScreenLayout.INSET_HEIGHT,
        FilterColumnLayout.Y + FilterColumnLayout.HEIGHT);
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
  void theHeaderControlsDoNotOverlapEachOther() {
    assertTrue(FilterColumnLayout.X + FilterColumnLayout.MODE_WIDTH <= FilterColumnLayout.CLEAR_X);
    assertTrue(
        FilterColumnLayout.CLEAR_X + FilterColumnLayout.CONTROL_SIZE
            <= FilterColumnLayout.X + FilterColumnLayout.WIDTH);
  }

  @Test
  void theHeaderControlsAnswerOnlyForTheirOwnRegion() {
    assertTrue(FilterColumnLayout.isOverMode(FilterColumnLayout.X + 1, FilterColumnLayout.Y + 1));
    assertFalse(FilterColumnLayout.isOverClear(FilterColumnLayout.X + 1, FilterColumnLayout.Y + 1));
    assertTrue(
        FilterColumnLayout.isOverClear(FilterColumnLayout.CLEAR_X + 1, FilterColumnLayout.Y + 1));
    assertFalse(
        FilterColumnLayout.isOverMode(FilterColumnLayout.CLEAR_X + 1, FilterColumnLayout.Y + 1));
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
