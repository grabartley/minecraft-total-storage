package com.grahambartley.toomanychests.screenhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.listing.ListingWindow;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class TerminalScreenLayoutTest {
  private static final int ITEM_SIZE = 16;

  static Stream<Integer> everySlot() {
    return IntStream.range(0, TerminalScreenLayout.SLOT_COUNT).boxed();
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

  @Test
  void theInventoryIsCentredInTheWindow() {
    int inventoryWidth = TerminalScreenLayout.COLUMNS * TerminalScreenLayout.SLOT_SIZE;
    int rightMargin =
        TerminalScreenLayout.WINDOW_WIDTH - (TerminalScreenLayout.x(0) + inventoryWidth);

    assertEquals(TerminalScreenLayout.x(0), rightMargin);
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
    assertTrue(
        TerminalScreenLayout.LISTING_COLUMNS * TerminalScreenLayout.SLOT_SIZE
            <= TerminalScreenLayout.INSET_WIDTH,
        "The cells across should fit inside the recess");
    assertTrue(
        TerminalScreenLayout.LISTING_ROWS * TerminalScreenLayout.SLOT_SIZE
            <= TerminalScreenLayout.INSET_HEIGHT,
        "The cells down should fit inside the recess");
  }
}
