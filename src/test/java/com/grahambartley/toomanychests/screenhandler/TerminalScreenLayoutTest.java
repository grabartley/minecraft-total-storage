package com.grahambartley.toomanychests.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.listing.ListingWindow;
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

  /**
   * Vanilla orders a player's inventory hotbar first, and every screen draws it last. Getting this
   * backwards puts a player's hotbar where their backpack should be, which no test of the geometry
   * alone would notice.
   */
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

  /**
   * Under the grid rather than under the recess or the window. Both of those are wider than the
   * grid by chrome, so centring on either leaves the inventory off to one side of the nine columns
   * directly above it.
   */
  @Test
  void theInventoryIsCentredUnderTheGrid() {
    int inventoryWidth = TerminalScreenLayout.COLUMNS * TerminalScreenLayout.SLOT_SIZE;
    int leftMargin = TerminalScreenLayout.x(0) - TerminalScreenLayout.GRID_X;
    int rightMargin =
        TerminalScreenLayout.GRID_X
            + TerminalScreenLayout.GRID_WIDTH
            - (TerminalScreenLayout.x(0) + inventoryWidth);

    assertEquals(leftMargin, rightMargin);
  }

  /** The recess the listing is drawn into must not run over the label under it. */
  @Test
  void theRecessedAreaClearsThePlayerInventoryLabel() {
    assertTrue(
        TerminalScreenLayout.INSET_Y + TerminalScreenLayout.INSET_HEIGHT
            <= TerminalScreenLayout.INVENTORY_LABEL_Y,
        "The inset ends at "
            + (TerminalScreenLayout.INSET_Y + TerminalScreenLayout.INSET_HEIGHT)
            + " but the label starts at "
            + TerminalScreenLayout.INVENTORY_LABEL_Y);
  }

  @Test
  void theRecessedAreaSitsInsideTheWindow() {
    assertTrue(
        TerminalScreenLayout.INSET_X + TerminalScreenLayout.INSET_WIDTH
            <= TerminalScreenLayout.WINDOW_WIDTH);
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

  /**
   * The chassis sprite's border is fixed at any window size and is drawn over everything under it.
   * A hotbar that merely fits inside the window has its bottom row of pixels, and the stack counts
   * hanging below it, drawn behind the frame.
   */
  @ParameterizedTest
  @MethodSource("everySlot")
  void everySlotClearsTheChassisFrameRatherThanMerelyFittingInTheWindow(int handlerIndex) {
    int x = TerminalScreenLayout.x(handlerIndex);
    int y = TerminalScreenLayout.y(handlerIndex);
    int border = TerminalScreenLayout.PANEL_BORDER;
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
    assertTrue(TerminalScreenLayout.TITLE_Y >= TerminalScreenLayout.PANEL_BORDER);
    assertTrue(TerminalScreenLayout.TITLE_X >= TerminalScreenLayout.PANEL_BORDER);
  }

  @Test
  void theRecessedAreaClearsTheChassisFrame() {
    assertTrue(TerminalScreenLayout.INSET_X >= TerminalScreenLayout.PANEL_BORDER);
    assertTrue(TerminalScreenLayout.INSET_Y >= TerminalScreenLayout.PANEL_BORDER);
    assertTrue(
        TerminalScreenLayout.INSET_X + TerminalScreenLayout.INSET_WIDTH
            <= TerminalScreenLayout.WINDOW_WIDTH - TerminalScreenLayout.PANEL_BORDER);
  }

  /** Vanilla drops to a smaller scale rather than clipping, so the window has to fit the floor. */
  @Test
  void theWindowFitsTheSmallestScreenVanillaWillDrawItAt() {
    assertTrue(TerminalScreenLayout.WINDOW_WIDTH <= 320, "wider than a 320x240 screen");
    assertTrue(TerminalScreenLayout.WINDOW_HEIGHT <= 240, "taller than a 320x240 screen");
  }

  @Test
  void theTitleSitsAboveTheRecessedArea() {
    assertTrue(TerminalScreenLayout.TITLE_Y + 9 <= TerminalScreenLayout.INSET_Y);
  }

  @Test
  void theSearchBoxSitsBetweenTheTitleAndTheRecessedArea() {
    assertTrue(
        TerminalScreenLayout.TITLE_Y + 9 <= TerminalScreenLayout.SEARCH_Y,
        "the search box overlaps the title");
    assertTrue(
        TerminalScreenLayout.SEARCH_Y + TerminalScreenLayout.SEARCH_HEIGHT
            <= TerminalScreenLayout.INSET_Y,
        "the search box overlaps the recess");
  }

  @Test
  void theSearchBoxSpansTheRecessItNarrows() {
    assertEquals(TerminalScreenLayout.INSET_X, TerminalScreenLayout.SEARCH_X);
    assertEquals(TerminalScreenLayout.INSET_WIDTH, TerminalScreenLayout.SEARCH_WIDTH);
    assertTrue(
        TerminalScreenLayout.SEARCH_X + TerminalScreenLayout.SEARCH_WIDTH
            <= TerminalScreenLayout.WINDOW_WIDTH - TerminalScreenLayout.PANEL_BORDER,
        "the search box runs under the chassis frame");
  }

  @Test
  void theToolbarSitsBesideTheRecessRatherThanOverIt() {
    assertTrue(
        TerminalScreenLayout.TOOLBAR_X
            >= TerminalScreenLayout.INSET_X + TerminalScreenLayout.INSET_WIDTH,
        "the toolbar overlaps the recess");
    assertEquals(TerminalScreenLayout.INSET_Y, TerminalScreenLayout.TOOLBAR_Y);
  }

  @Test
  void aToolbarButtonIsDrawnInsideTheChassisFrame() {
    assertTrue(
        TerminalScreenLayout.TOOLBAR_X + TerminalScreenLayout.BUTTON_SIZE
            <= TerminalScreenLayout.WINDOW_WIDTH - TerminalScreenLayout.PANEL_BORDER,
        "the button runs under the chassis frame");
    assertTrue(
        TerminalScreenLayout.TOOLBAR_Y >= TerminalScreenLayout.PANEL_BORDER,
        "the button runs under the chassis frame");
  }

  /** A column added by widening the window keeps the margins either side of it equal. */
  @Test
  void theToolbarIsGivenRoomWithoutUnbalancingTheWindow() {
    assertEquals(
        TerminalScreenLayout.INSET_X,
        TerminalScreenLayout.WINDOW_WIDTH
            - (TerminalScreenLayout.TOOLBAR_X + TerminalScreenLayout.BUTTON_SIZE));
  }

  /** An icon that merely fits its button reads as pinned to a corner rather than centred in it. */
  @Test
  void anIconCentresInTheButtonAroundIt() {
    assertEquals(
        TerminalScreenLayout.BUTTON_SIZE,
        2 * TerminalScreenLayout.ICON_INSET + TerminalScreenLayout.ICON_SIZE);
  }

  /**
   * The text is a widget of its own, so what it covers has to stay inside the box drawn behind it.
   */
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

  /**
   * The border counts, so a click that visibly lands on the control reaches the field inside it.
   */
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

  static Stream<Integer> everyTab() {
    return IntStream.range(0, TerminalScreenLayout.TAB_COUNT).boxed();
  }

  @ParameterizedTest
  @MethodSource("everyTab")
  void everyTabIsDrawnInTheToolbarColumn(int tab) {
    int top = TerminalScreenLayout.tabY(tab);

    assertTrue(top >= TerminalScreenLayout.TOOLBAR_Y, "tab at " + top);
    assertTrue(
        top + TerminalScreenLayout.BUTTON_SIZE <= TerminalScreenLayout.ACTIONS_Y,
        "the tab strip runs into the actions below it");
  }

  @Test
  void theTabsTouchSoTheyReadAsOneStrip() {
    assertEquals(
        TerminalScreenLayout.tabY(0) + TerminalScreenLayout.BUTTON_SIZE,
        TerminalScreenLayout.tabY(1));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 2, 100})
  void anIndexThatIsNotATabIsRefusedRatherThanGuessedAt(int tab) {
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalScreenLayout.tabY(tab));
  }

  /** The toolbar has to hold both actions a view needs without running past the recess. */
  @Test
  void theToolbarHasRoomForTheActionsBesideTheStrip() {
    int last = TerminalScreenLayout.actionY(1) + TerminalScreenLayout.BUTTON_SIZE;

    assertTrue(
        last <= TerminalScreenLayout.INSET_Y + TerminalScreenLayout.INSET_HEIGHT,
        "the actions run past the recess, ending at " + last);
  }

  @Test
  void theActionsAreSeparatedFromTheStripTheySitUnder() {
    assertTrue(
        TerminalScreenLayout.actionY(0)
            >= TerminalScreenLayout.tabY(TerminalScreenLayout.TAB_COUNT - 1)
                + TerminalScreenLayout.BUTTON_SIZE,
        "the first action overlaps the last tab");
  }

  @Test
  void aClickBesideTheToolbarIsOverNoTab() {
    assertEquals(
        TerminalScreenLayout.NO_CELL,
        TerminalScreenLayout.tabAt(
            TerminalScreenLayout.TOOLBAR_X - 1, TerminalScreenLayout.TOOLBAR_Y));
    assertEquals(
        TerminalScreenLayout.NO_CELL,
        TerminalScreenLayout.tabAt(TerminalScreenLayout.GRID_X, TerminalScreenLayout.GRID_Y));
  }

  /**
   * The settings panel and the grid share one recess, so it must cover exactly what the grid does.
   */
  @Test
  void theSettingsPanelFillsTheRecessTheGridUses() {
    assertEquals(TerminalScreenLayout.GRID_X, TerminalScreenLayout.SETTINGS_X);
    assertEquals(TerminalScreenLayout.GRID_Y, TerminalScreenLayout.SETTINGS_Y);
    assertEquals(TerminalScreenLayout.GRID_HEIGHT, TerminalScreenLayout.SETTINGS_HEIGHT);
    assertEquals(
        TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalScreenLayout.SETTINGS_X + TerminalScreenLayout.SETTINGS_WIDTH);
  }

  @Test
  void theContainerListStartsBelowTheHeaderAndEndsInsideTheRecess() {
    assertEquals(
        TerminalScreenLayout.settingsRowY(TerminalScreenLayout.SETTINGS_HEADER_ROWS),
        TerminalScreenLayout.CONTAINER_LIST_Y);
    assertTrue(
        TerminalScreenLayout.containerRowY(TerminalScreenLayout.CONTAINER_ROWS - 1)
                + TerminalScreenLayout.CONTAINER_ROW_HEIGHT
            <= TerminalScreenLayout.SETTINGS_Y + TerminalScreenLayout.SETTINGS_HEIGHT,
        "the last container row runs past the recess");
  }

  @Test
  void theContainerListHasRoomForAtLeastAFewRows() {
    assertTrue(
        TerminalScreenLayout.CONTAINER_ROWS >= 3,
        "a list of " + TerminalScreenLayout.CONTAINER_ROWS + " rows is not navigable");
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, 100})
  void anIndexThatIsNotAContainerRowIsRefusedRatherThanGuessedAt(int row) {
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalScreenLayout.containerRowY(row));
  }

  /**
   * The header's last row is the one the container list starts on, so it is addressable; anything
   * past it is not.
   */
  @Test
  void anIndexThatIsNotASettingsRowIsRefusedRatherThanGuessedAt() {
    assertThrows(IndexOutOfBoundsException.class, () -> TerminalScreenLayout.settingsRowY(-1));
    assertThrows(
        IndexOutOfBoundsException.class,
        () -> TerminalScreenLayout.settingsRowY(TerminalScreenLayout.SETTINGS_HEADER_ROWS + 1));
    assertEquals(
        TerminalScreenLayout.CONTAINER_LIST_Y,
        TerminalScreenLayout.settingsRowY(TerminalScreenLayout.SETTINGS_HEADER_ROWS));
  }

  @Test
  void aClickAboveOrBesideTheContainerListIsOverNoRow() {
    assertEquals(
        TerminalScreenLayout.NO_CELL,
        TerminalScreenLayout.containerRowAt(
            TerminalScreenLayout.SETTINGS_X, TerminalScreenLayout.CONTAINER_LIST_Y - 1));
    assertEquals(
        TerminalScreenLayout.NO_CELL,
        TerminalScreenLayout.containerRowAt(
            TerminalScreenLayout.SETTINGS_X - 1, TerminalScreenLayout.CONTAINER_LIST_Y));
    assertEquals(
        TerminalScreenLayout.NO_CELL,
        TerminalScreenLayout.containerRowAt(
            TerminalScreenLayout.SETTINGS_X + TerminalScreenLayout.SETTINGS_WIDTH,
            TerminalScreenLayout.CONTAINER_LIST_Y));
  }

  /** The gutter belongs to the scrollbar, so a click there must not land on the row behind it. */
  @Test
  void aClickOnTheContainerScrollbarIsNotARow() {
    assertEquals(
        TerminalScreenLayout.NO_CELL,
        TerminalScreenLayout.containerRowAt(
            TerminalScreenLayout.SCROLLBAR_X, TerminalScreenLayout.CONTAINER_LIST_Y));
    assertTrue(
        TerminalScreenLayout.isOverContainerScrollbar(
            TerminalScreenLayout.SCROLLBAR_X, TerminalScreenLayout.CONTAINER_LIST_Y));
  }

  @Test
  void theContainerRowsLeaveTheGutterToTheirScrollbar() {
    assertTrue(
        TerminalScreenLayout.SETTINGS_X + TerminalScreenLayout.CONTAINER_ROW_WIDTH
            <= TerminalScreenLayout.SCROLLBAR_X,
        "the rows run under the scrollbar");
  }

  @Test
  void aPointBesideTheContainerScrollbarIsNotOverIt() {
    assertFalse(
        TerminalScreenLayout.isOverContainerScrollbar(
            TerminalScreenLayout.SCROLLBAR_X - 1, TerminalScreenLayout.CONTAINER_LIST_Y));
    assertFalse(
        TerminalScreenLayout.isOverContainerScrollbar(
            TerminalScreenLayout.SCROLLBAR_X, TerminalScreenLayout.CONTAINER_LIST_Y - 1));
    assertFalse(
        TerminalScreenLayout.isOverContainerScrollbar(
            TerminalScreenLayout.SCROLLBAR_X,
            TerminalScreenLayout.CONTAINER_LIST_Y + TerminalScreenLayout.CONTAINER_LIST_HEIGHT));
  }

  @ParameterizedTest
  @MethodSource("everyContainerRow")
  void everyContainerRowIsClickableWhereItIsDrawn(int row) {
    assertEquals(
        row,
        TerminalScreenLayout.containerRowAt(
            TerminalScreenLayout.SETTINGS_X, TerminalScreenLayout.containerRowY(row)));
  }

  static Stream<Integer> everyContainerRow() {
    return IntStream.range(0, TerminalScreenLayout.CONTAINER_ROWS).boxed();
  }

  /**
   * A page is asked for one recess at a time, and a request for more rows than a page may carry is
   * clamped rather than refused. A recess grown past the cap would therefore ask for cells it is
   * never sent, and draw the difference as blanks.
   */
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
        TerminalScreenLayout.LISTING_COLUMNS * TerminalScreenLayout.CELL_SIZE,
        TerminalScreenLayout.GRID_WIDTH);
    assertEquals(
        TerminalScreenLayout.LISTING_ROWS * TerminalScreenLayout.CELL_SIZE,
        TerminalScreenLayout.GRID_HEIGHT);
  }

  /**
   * The recess sprite's border is fixed at any size and is drawn over its own contents, so a grid
   * that merely fits inside the recess has its outermost cells drawn behind the frame.
   */
  @Test
  void theGridAndItsScrollbarClearTheRecessFrame() {
    assertTrue(
        TerminalScreenLayout.GRID_X - TerminalScreenLayout.INSET_X
            >= TerminalScreenLayout.INSET_BORDER,
        "The grid starts under the recess frame");
    assertTrue(
        TerminalScreenLayout.GRID_Y - TerminalScreenLayout.INSET_Y
            >= TerminalScreenLayout.INSET_BORDER,
        "The grid starts under the recess frame");
    assertTrue(
        TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH
            <= TerminalScreenLayout.INSET_X
                + TerminalScreenLayout.INSET_WIDTH
                - TerminalScreenLayout.INSET_BORDER,
        "The scrollbar ends under the recess frame");
    assertTrue(
        TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT
            <= TerminalScreenLayout.INSET_Y
                + TerminalScreenLayout.INSET_HEIGHT
                - TerminalScreenLayout.INSET_BORDER,
        "The grid ends under the recess frame");
  }

  /** A window sized around the grid has no strip of dead space beside it. */
  @Test
  void theRecessIsExactlyAsWideAsWhatItHolds() {
    assertEquals(
        2 * TerminalScreenLayout.INSET_BORDER
            + TerminalScreenLayout.GRID_WIDTH
            + TerminalScreenLayout.SCROLLBAR_GAP
            + TerminalScreenLayout.SCROLLBAR_WIDTH,
        TerminalScreenLayout.INSET_WIDTH);
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
        cellX + TerminalScreenLayout.CELL_SIZE
            <= TerminalScreenLayout.GRID_X + TerminalScreenLayout.GRID_WIDTH,
        "right edge at " + (cellX + TerminalScreenLayout.CELL_SIZE));
    assertTrue(
        cellY + TerminalScreenLayout.CELL_SIZE
            <= TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT,
        "bottom edge at " + (cellY + TerminalScreenLayout.CELL_SIZE));
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

  /**
   * The hit test is the drawing arithmetic run backwards, so proving it against every cell's own
   * corners is what stops a cell a player can see but cannot click.
   */
  @ParameterizedTest
  @MethodSource("everyCell")
  void everyCellIsFoundAtItsOwnCorners(int cell) {
    int cellX = TerminalScreenLayout.cellX(cell);
    int cellY = TerminalScreenLayout.cellY(cell);
    int last = TerminalScreenLayout.CELL_SIZE - 1;

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

  /**
   * The gap between the grid and the track is two pixels of nothing, and a wheel ignored over it
   * would be a strip of the listing that does not scroll.
   */
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

  /** Every cell of the grid is part of the listing the wheel scrolls. */
  @ParameterizedTest
  @MethodSource("everyCell")
  void everyCellIsPartOfTheListing(int cell) {
    assertTrue(
        TerminalScreenLayout.isOverListing(
            TerminalScreenLayout.cellX(cell), TerminalScreenLayout.cellY(cell)));
  }

  /** A 16x16 item plus its two pixel bevel and gutter is exactly what a cell was sized for. */
  @Test
  void anItemDrawnAtTheCellOffsetSitsInsideItsCell() {
    assertEquals(
        TerminalScreenLayout.CELL_SIZE, 2 * TerminalScreenLayout.CELL_ITEM_OFFSET + ITEM_SIZE);
  }
}
