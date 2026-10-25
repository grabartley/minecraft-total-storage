package com.grahambartley.totalstorage.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.listing.ListingWindow;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalScreenLayoutTest {
  private static final int ITEM_SIZE = 16;

  private static final int VANILLA_SLOT_SIZE = 18;

  private static final int VANILLA_PANEL_BORDER = 4;

  private static final int VANILLA_SLOT_ITEM_OFFSET = 1;

  static Stream<Integer> everySlot() {
    return IntStream.range(0, TerminalScreenLayout.SLOT_COUNT).boxed();
  }

  static Stream<Integer> everyCell() {
    return IntStream.range(0, TerminalScreenLayout.LISTING_PAGE_SIZE).boxed();
  }

  static Stream<Arguments> pointsOutsideTheGrid() {
    final int right = TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH;
    final int bottom = TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT;
    return Stream.of(
        Arguments.of(TerminalScreenLayout.GRID_X - 1, TerminalScreenLayout.GRID_Y),
        Arguments.of(TerminalScreenLayout.GRID_X, TerminalScreenLayout.GRID_Y - 1),
        Arguments.of(right, TerminalScreenLayout.GRID_Y),
        Arguments.of(TerminalScreenLayout.GRID_X, bottom),
        Arguments.of(TerminalScreenLayout.SCROLLBAR_X, TerminalScreenLayout.SCROLLBAR_Y),
        Arguments.of(0, 0));
  }

  @Test
  void theHandlerCarriesTheWholePlayerInventoryAndNothingElse() {
    assertEquals(36, TerminalScreenLayout.SLOT_COUNT);
  }

  @Test
  void everyHandlerSlotStandsForADifferentInventorySlot() {
    Set<Integer> inventorySlots = new HashSet<>();

    everySlot().forEach(index -> inventorySlots.add(TerminalScreenLayout.inventoryIndex(index)));

    assertEquals(
        IntStream.range(0, TerminalScreenLayout.SLOT_COUNT)
            .boxed()
            .collect(java.util.stream.Collectors.toSet()),
        inventorySlots);
  }

  @ParameterizedTest
  @ValueSource(ints = {27, 31, 35})
  void theHotbarMapsToTheFirstNineInventorySlots(int handlerIndex) {
    assertEquals(
        handlerIndex - TerminalScreenLayout.HOTBAR_START,
        TerminalScreenLayout.inventoryIndex(handlerIndex));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, 13, 26})
  void theMainInventoryMapsToTheSlotsAboveTheHotbar(int handlerIndex) {
    assertEquals(
        handlerIndex + TerminalScreenLayout.COLUMNS,
        TerminalScreenLayout.inventoryIndex(handlerIndex));
  }

  @ParameterizedTest
  @MethodSource("everySlot")
  void everySlotIsDrawnInsideTheWindow(int handlerIndex) {
    int x = TerminalScreenLayout.x(handlerIndex);
    int y = TerminalScreenLayout.y(handlerIndex);

    assertTrue(x >= 0 && x + ITEM_SIZE <= TerminalScreenLayout.WINDOW_WIDTH, "x was " + x);
    assertTrue(y >= 0 && y + ITEM_SIZE <= TerminalScreenLayout.WINDOW_HEIGHT, "y was " + y);
  }

  @Test
  void noTwoSlotsShareAPosition() {
    Set<String> positions = new HashSet<>();

    everySlot()
        .forEach(
            index ->
                positions.add(TerminalScreenLayout.x(index) + "," + TerminalScreenLayout.y(index)));

    assertEquals(TerminalScreenLayout.SLOT_COUNT, positions.size());
  }

  @Test
  void theInventoryIsLaidOutInNineColumns() {
    Set<Integer> columns = new HashSet<>();

    everySlot().forEach(index -> columns.add(TerminalScreenLayout.x(index)));

    assertEquals(TerminalScreenLayout.COLUMNS, columns.size());
  }

  @Test
  void theHotbarSitsBelowTheMainInventoryWithAGap() {
    int lastMainRow = TerminalScreenLayout.y(TerminalScreenLayout.HOTBAR_START - 1);

    assertEquals(
        lastMainRow + TerminalScreenLayout.SLOT_SIZE + TerminalScreenLayout.HOTBAR_GAP,
        TerminalScreenLayout.y(TerminalScreenLayout.HOTBAR_START));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 36, 100})
  void anIndexThatIsNotASlotIsRefusedRatherThanGuessedAt(int handlerIndex) {
    assertThrows(
        IndexOutOfBoundsException.class, () -> TerminalScreenLayout.inventoryIndex(handlerIndex));
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalScreenLayout.x(handlerIndex));
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalScreenLayout.y(handlerIndex));
  }

  @ParameterizedTest
  @MethodSource("everySlot")
  void onlyTheLastRowIsTheHotbar(int handlerIndex) {
    assertEquals(
        handlerIndex >= TerminalScreenLayout.HOTBAR_START,
        TerminalScreenLayout.isHotbar(handlerIndex));
  }

  @ParameterizedTest
  @MethodSource("everySlot")
  void everySlotClearsTheChassisFrameRatherThanMerelyFittingInTheWindow(int handlerIndex) {
    int x = TerminalScreenLayout.x(handlerIndex);
    int y = TerminalScreenLayout.y(handlerIndex);
    int border = TerminalScreenLayout.EDGE;
    int size = TerminalScreenLayout.SLOT_SIZE;

    assertTrue(x - 1 >= border, "left edge at " + (x - 1) + " is under the frame");
    assertTrue(
        x - 1 + size <= TerminalScreenLayout.WINDOW_WIDTH - border,
        "right edge at " + (x - 1 + size) + " is under the frame");
    assertTrue(y - 1 >= border, "top edge at " + (y - 1) + " is under the frame");
    assertTrue(
        y - 1 + size <= TerminalScreenLayout.WINDOW_HEIGHT - border,
        "bottom edge at " + (y - 1 + size) + " is under the frame");
  }

  @Test
  void theTitleClearsTheChassisFrame() {
    assertTrue(TerminalScreenLayout.TITLE_Y >= VANILLA_PANEL_BORDER);
    assertTrue(TerminalScreenLayout.TITLE_X >= VANILLA_PANEL_BORDER);
  }

  @Test
  void theWindowFitsTheSmallestScreenVanillaWillDrawItAt() {
    assertTrue(TerminalScreenLayout.WINDOW_WIDTH <= 320, "wider than a 320x240 screen");
    assertTrue(TerminalScreenLayout.WINDOW_HEIGHT <= 240, "taller than a 320x240 screen");
  }

  @Test
  void theSearchTextClearsTheBoxItIsWrittenInto() {
    assertTrue(TerminalScreenLayout.SEARCH_TEXT_X >= TerminalScreenLayout.SEARCH_X);
    assertTrue(TerminalScreenLayout.SEARCH_TEXT_Y >= TerminalScreenLayout.SEARCH_Y);
    assertTrue(
        TerminalScreenLayout.SEARCH_TEXT_X + TerminalScreenLayout.SEARCH_TEXT_WIDTH
            <= TerminalScreenLayout.SEARCH_X + TerminalScreenLayout.SEARCH_WIDTH);
    assertTrue(
        TerminalScreenLayout.SEARCH_TEXT_Y + TerminalScreenLayout.SEARCH_TEXT_HEIGHT
            <= TerminalScreenLayout.SEARCH_Y + TerminalScreenLayout.SEARCH_HEIGHT);
    assertTrue(TerminalScreenLayout.SEARCH_TEXT_HEIGHT >= 8, "a line of text does not fit the box");
  }

  static Stream<Arguments> pointsOutsideTheSearchBox() {
    final int right = TerminalScreenLayout.SEARCH_X + TerminalScreenLayout.SEARCH_WIDTH;
    final int bottom = TerminalScreenLayout.SEARCH_Y + TerminalScreenLayout.SEARCH_HEIGHT;
    return Stream.of(
        Arguments.of(TerminalScreenLayout.SEARCH_X - 1, TerminalScreenLayout.SEARCH_Y),
        Arguments.of(TerminalScreenLayout.SEARCH_X, TerminalScreenLayout.SEARCH_Y - 1),
        Arguments.of(right, TerminalScreenLayout.SEARCH_Y),
        Arguments.of(TerminalScreenLayout.SEARCH_X, bottom),
        Arguments.of(TerminalScreenLayout.GRID_X, TerminalScreenLayout.GRID_Y));
  }

  @ParameterizedTest
  @MethodSource("pointsOutsideTheSearchBox")
  void aClickBesideTheSearchBoxIsNotOverIt(int windowX, int windowY) {
    assertFalse(TerminalScreenLayout.isOverSearch(windowX, windowY));
  }

  @Test
  void aClickAnywhereOnTheSearchBoxIsOverIt() {
    assertTrue(
        TerminalScreenLayout.isOverSearch(
            TerminalScreenLayout.SEARCH_X, TerminalScreenLayout.SEARCH_Y));
    assertTrue(
        TerminalScreenLayout.isOverSearch(
            TerminalScreenLayout.SEARCH_TEXT_X, TerminalScreenLayout.SEARCH_TEXT_Y));
    assertTrue(
        TerminalScreenLayout.isOverSearch(
            TerminalScreenLayout.SEARCH_X + TerminalScreenLayout.SEARCH_WIDTH - 1,
            TerminalScreenLayout.SEARCH_Y + TerminalScreenLayout.SEARCH_HEIGHT - 1));
  }

  @Test
  void aRecessNeverAsksForMoreRowsThanAPageMayCarry() {
    assertTrue(
        TerminalScreenLayout.LISTING_PAGE_SIZE <= ListingWindow.MAX_LENGTH,
        "A recess of "
            + TerminalScreenLayout.LISTING_PAGE_SIZE
            + " cells cannot be filled by a page capped at "
            + ListingWindow.MAX_LENGTH);
  }

  @Test
  void theRecessHoldsAWholeNumberOfCellsInBothDirections() {
    assertEquals(
        TerminalScreenLayout.LISTING_PAGE_SIZE,
        TerminalScreenLayout.LISTING_COLUMNS * TerminalScreenLayout.LISTING_ROWS);
    assertEquals(
        TerminalScreenLayout.LISTING_COLUMNS * TerminalScreenLayout.SLOT_SIZE,
        TerminalScreenLayout.GRID_WIDTH);
    assertEquals(
        TerminalScreenLayout.LISTING_ROWS * TerminalScreenLayout.SLOT_SIZE,
        TerminalScreenLayout.GRID_HEIGHT);
  }

  @Test
  void theScrollbarSitsBesideTheGridRatherThanOverIt() {
    assertTrue(
        TerminalScreenLayout.SCROLLBAR_X
            >= TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH,
        "The scrollbar overlaps the last column of cells");
    assertEquals(TerminalScreenLayout.GRID_Y, TerminalScreenLayout.SCROLLBAR_Y);
    assertEquals(TerminalScreenLayout.GRID_HEIGHT, TerminalScreenLayout.SCROLLBAR_HEIGHT);
  }

  @ParameterizedTest
  @MethodSource("everyCell")
  void everyCellIsDrawnInsideTheGrid(int cell) {
    int cellX = TerminalScreenLayout.cellX(cell);
    int cellY = TerminalScreenLayout.cellY(cell);

    assertTrue(cellX >= TerminalScreenLayout.GRID_X, "left edge at " + cellX);
    assertTrue(cellY >= TerminalScreenLayout.GRID_Y, "top edge at " + cellY);
    assertTrue(
        cellX + TerminalScreenLayout.SLOT_SIZE
            <= TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH,
        "right edge at " + (cellX + TerminalScreenLayout.SLOT_SIZE));
    assertTrue(
        cellY + TerminalScreenLayout.SLOT_SIZE
            <= TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT,
        "bottom edge at " + (cellY + TerminalScreenLayout.SLOT_SIZE));
  }

  @Test
  void noTwoCellsShareAPosition() {
    Set<String> positions = new HashSet<>();

    everyCell()
        .forEach(
            cell ->
                positions.add(
                    TerminalScreenLayout.cellX(cell) + "," + TerminalScreenLayout.cellY(cell)));

    assertEquals(TerminalScreenLayout.LISTING_PAGE_SIZE, positions.size());
  }

  @ParameterizedTest
  @MethodSource("everyCell")
  void everyCellIsFoundAtItsOwnCorners(int cell) {
    int cellX = TerminalScreenLayout.cellX(cell);
    int cellY = TerminalScreenLayout.cellY(cell);
    int last = TerminalScreenLayout.SLOT_SIZE - 1;

    assertEquals(cell, TerminalScreenLayout.cellAt(cellX, cellY));
    assertEquals(cell, TerminalScreenLayout.cellAt(cellX + last, cellY));
    assertEquals(cell, TerminalScreenLayout.cellAt(cellX, cellY + last));
    assertEquals(cell, TerminalScreenLayout.cellAt(cellX + last, cellY + last));
  }

  @DisplayName("a point beside the grid is no cell rather than the nearest one")
  @ParameterizedTest(name = "({0}, {1})")
  @MethodSource("pointsOutsideTheGrid")
  void aPointOutsideTheGridIsOverNoCell(int windowX, int windowY) {
    assertEquals(TerminalScreenLayout.NO_CELL, TerminalScreenLayout.cellAt(windowX, windowY));
  }

  @Test
  void theScrollbarIsFoundAtItsOwnCornersAndNowhereElse() {
    assertTrue(
        TerminalScreenLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X, TerminalScreenLayout.SCROLLBAR_Y));
    assertTrue(
        TerminalScreenLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH - 1,
            TerminalScreenLayout.SCROLLBAR_Y + TerminalScreenLayout.SCROLLBAR_HEIGHT - 1));
    assertFalse(
        TerminalScreenLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X - 1, TerminalScreenLayout.SCROLLBAR_Y));
    assertFalse(
        TerminalScreenLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH,
            TerminalScreenLayout.SCROLLBAR_Y));
    assertFalse(
        TerminalScreenLayout.isOverScrollbar(
            TerminalScreenLayout.SCROLLBAR_X,
            TerminalScreenLayout.SCROLLBAR_Y + TerminalScreenLayout.SCROLLBAR_HEIGHT));
  }

  @DisplayName("an index that is not a cell is refused rather than guessed at")
  @ParameterizedTest(name = "cell {0}")
  @ValueSource(ints = {-1, 45, 1000})
  void anIndexThatIsNotACellIsRefused(int cell) {
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalScreenLayout.cellX(cell));
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalScreenLayout.cellY(cell));
  }

  @Test
  void theGapBetweenTheGridAndItsScrollbarIsStillTheListing() {
    int gap = TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH;

    assertEquals(
        TerminalScreenLayout.NO_CELL,
        TerminalScreenLayout.cellAt(gap, TerminalScreenLayout.GRID_Y));
    assertFalse(TerminalScreenLayout.isOverScrollbar(gap, TerminalScreenLayout.GRID_Y));
    assertTrue(TerminalScreenLayout.isOverListing(gap, TerminalScreenLayout.GRID_Y));
  }

  @Test
  void theListingCoversTheGridAndItsScrollbarAndNothingElse() {
    assertTrue(
        TerminalScreenLayout.isOverListing(
            TerminalScreenLayout.GRID_X, TerminalScreenLayout.GRID_Y));
    assertTrue(
        TerminalScreenLayout.isOverListing(
            TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH - 1,
            TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT - 1));
    assertFalse(
        TerminalScreenLayout.isOverListing(
            TerminalScreenLayout.GRID_X - 1, TerminalScreenLayout.GRID_Y));
    assertFalse(
        TerminalScreenLayout.isOverListing(
            TerminalScreenLayout.GRID_X, TerminalScreenLayout.GRID_Y - 1));
    assertFalse(
        TerminalScreenLayout.isOverListing(
            TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH,
            TerminalScreenLayout.GRID_Y));
    assertFalse(
        TerminalScreenLayout.isOverListing(
            TerminalScreenLayout.GRID_X,
            TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT));
  }

  @ParameterizedTest
  @MethodSource("everyCell")
  void everyCellIsPartOfTheListing(int cell) {
    assertTrue(
        TerminalScreenLayout.isOverListing(
            TerminalScreenLayout.cellX(cell), TerminalScreenLayout.cellY(cell)));
  }

  @Test
  void anItemDrawnAtTheCellOffsetSitsInsideItsCell() {
    assertEquals(
        TerminalScreenLayout.SLOT_SIZE, 2 * TerminalScreenLayout.SLOT_ITEM_OFFSET + ITEM_SIZE);
  }

  @Test
  void aGridCellIsAVanillaSlot() {
    assertEquals(VANILLA_SLOT_SIZE, TerminalScreenLayout.SLOT_SIZE);
    assertEquals(VANILLA_SLOT_ITEM_OFFSET, TerminalScreenLayout.SLOT_ITEM_OFFSET);
  }

  @Test
  void theGridAndThePlayerInventoryShareTheirColumns() {
    assertEquals(
        TerminalScreenLayout.COLUMNS * TerminalScreenLayout.SLOT_SIZE,
        TerminalScreenLayout.GRID_WIDTH);
    for (int column = 0; column < TerminalScreenLayout.COLUMNS; column++) {
      assertEquals(
          TerminalScreenLayout.cellX(column),
          TerminalScreenLayout.x(column) - TerminalScreenLayout.SLOT_ITEM_OFFSET,
          "the slot frame of grid column " + column + " is not above the inventory frame below it");
      assertEquals(
          TerminalScreenLayout.cellX(column) + TerminalScreenLayout.SLOT_ITEM_OFFSET,
          TerminalScreenLayout.x(column),
          "the item in grid column " + column + " is not above the item in the inventory below it");
    }
  }

  @Test
  void theSearchBoxSitsBetweenTheTitleAndTheGrid() {
    assertTrue(
        TerminalScreenLayout.TITLE_Y + 9 <= TerminalScreenLayout.SEARCH_Y,
        "the search box overlaps the title");
    assertTrue(
        TerminalScreenLayout.SEARCH_Y + TerminalScreenLayout.SEARCH_HEIGHT
            < TerminalScreenLayout.GRID_Y,
        "the search box touches the grid");
  }

  @Test
  void theSearchBoxSpansTheGridAndItsScrollbar() {
    assertEquals(TerminalScreenLayout.GRID_X, TerminalScreenLayout.SEARCH_X);
    assertEquals(
        TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalScreenLayout.SEARCH_X + TerminalScreenLayout.SEARCH_WIDTH);
  }

  @Test
  void theListingKeepsTheSameMarginFromTheRightEdgeAsTheSideColumnFromTheLeft() {
    assertEquals(
        TerminalScreenLayout.SIDE_X,
        TerminalScreenLayout.WINDOW_WIDTH
            - (TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH));
  }

  @Test
  void theSideColumnEndsBeforeTheGridBegins() {
    assertTrue(
        TerminalScreenLayout.SIDE_X + TerminalScreenLayout.SIDE_WIDTH
            < TerminalScreenLayout.GRID_X);
  }

  @Test
  void theSearchIconAndTextSitInsideTheSearchBoxWithoutTouching() {
    assertTrue(TerminalScreenLayout.SEARCH_ICON_X > TerminalScreenLayout.SEARCH_X);
    assertTrue(TerminalScreenLayout.SEARCH_ICON_Y >= TerminalScreenLayout.SEARCH_Y);
    assertTrue(
        TerminalScreenLayout.SEARCH_ICON_Y + TerminalScreenLayout.SEARCH_ICON_SIZE
            <= TerminalScreenLayout.SEARCH_Y + TerminalScreenLayout.SEARCH_HEIGHT);
    assertTrue(
        TerminalScreenLayout.SEARCH_TEXT_X
            > TerminalScreenLayout.SEARCH_ICON_X + TerminalScreenLayout.SEARCH_ICON_SIZE,
        "the search text touches its icon");
  }

  @Test
  void theInventoryLabelSitsBetweenTheGridAndTheInventory() {
    assertTrue(
        TerminalScreenLayout.INVENTORY_LABEL_Y
            > TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT,
        "the label touches the grid");
    assertTrue(
        TerminalScreenLayout.INVENTORY_LABEL_Y + 9
            < TerminalScreenLayout.MAIN_INVENTORY_Y - TerminalScreenLayout.SLOT_ITEM_OFFSET,
        "the label touches the inventory");
  }

  @Test
  void theStatusLineReplacesTheInventoryLabelAcrossTheListingWidth() {
    assertEquals(TerminalScreenLayout.INVENTORY_LABEL_X, TerminalScreenLayout.STATUS_X);
    assertEquals(TerminalScreenLayout.INVENTORY_LABEL_Y, TerminalScreenLayout.STATUS_Y);
    assertEquals(TerminalScreenLayout.SEARCH_WIDTH, TerminalScreenLayout.STATUS_WIDTH);
  }

  @Test
  void theStatusLineIsFoundAtItsOwnCornersAndNowhereElse() {
    final int right = TerminalScreenLayout.STATUS_X + TerminalScreenLayout.STATUS_WIDTH;
    final int bottom = TerminalScreenLayout.STATUS_Y + TerminalScreenLayout.STATUS_HEIGHT;

    assertTrue(
        TerminalScreenLayout.isOverStatus(
            TerminalScreenLayout.STATUS_X, TerminalScreenLayout.STATUS_Y));
    assertTrue(TerminalScreenLayout.isOverStatus(right - 1, bottom - 1));
    assertFalse(
        TerminalScreenLayout.isOverStatus(
            TerminalScreenLayout.STATUS_X - 1, TerminalScreenLayout.STATUS_Y));
    assertFalse(TerminalScreenLayout.isOverStatus(right, TerminalScreenLayout.STATUS_Y));
    assertFalse(TerminalScreenLayout.isOverStatus(TerminalScreenLayout.STATUS_X, bottom));
  }

  @Test
  void theHotbarEndsAboveTheBottomEdge() {
    assertTrue(
        TerminalScreenLayout.BOTTOM_Y
            <= TerminalScreenLayout.WINDOW_HEIGHT - TerminalScreenLayout.EDGE,
        "the hotbar runs into the bottom edge at " + TerminalScreenLayout.BOTTOM_Y);
  }
}
