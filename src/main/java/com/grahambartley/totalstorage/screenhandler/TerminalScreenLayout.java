package com.grahambartley.totalstorage.screenhandler;

public final class TerminalScreenLayout {

  public static final int COLUMNS = 9;
  public static final int MAIN_ROWS = 3;
  public static final int SLOT_SIZE = 18;

  public static final int SLOT_ITEM_OFFSET = 1;

  public static final int LISTING_COLUMNS = 9;
  public static final int LISTING_ROWS = 5;
  public static final int LISTING_PAGE_SIZE = LISTING_COLUMNS * LISTING_ROWS;

  public static final int NO_CELL = -1;

  public static final int GRID_WIDTH = LISTING_COLUMNS * SLOT_SIZE;
  public static final int GRID_HEIGHT = LISTING_ROWS * SLOT_SIZE;

  public static final int MAIN_SLOT_COUNT = MAIN_ROWS * COLUMNS;

  public static final int HOTBAR_START = MAIN_SLOT_COUNT;
  public static final int SLOT_COUNT = HOTBAR_START + COLUMNS;

  public static final int WINDOW_WIDTH = 278;

  public static final int WINDOW_HEIGHT = 222;

  public static final int EDGE = 7;

  public static final int TITLE_X = 8;
  public static final int TITLE_Y = 6;

  public static final int SIDE_WIDTH = 84;

  public static final int SIDE_X = EDGE;

  public static final int COLUMN_GAP = 4;

  public static final int HEADER_Y = 17;

  public static final int HEADER_HEIGHT = 14;

  public static final int GRID_X = SIDE_X + SIDE_WIDTH + COLUMN_GAP;

  public static final int GRID_Y = HEADER_Y + HEADER_HEIGHT + 2;

  public static final int SCROLLBAR_GAP = 2;

  public static final int SCROLLBAR_WIDTH = 12;

  public static final int SCROLLBAR_X = GRID_X + GRID_WIDTH + SCROLLBAR_GAP;
  public static final int SCROLLBAR_Y = GRID_Y;
  public static final int SCROLLBAR_HEIGHT = GRID_HEIGHT;

  public static final int SEARCH_X = GRID_X;
  public static final int SEARCH_Y = HEADER_Y;
  public static final int SEARCH_WIDTH = SCROLLBAR_X + SCROLLBAR_WIDTH - GRID_X;
  public static final int SEARCH_HEIGHT = HEADER_HEIGHT;

  public static final int SEARCH_ICON_SIZE = 12;

  public static final int SEARCH_ICON_X = SEARCH_X + 2;

  public static final int SEARCH_ICON_Y = SEARCH_Y + (SEARCH_HEIGHT - SEARCH_ICON_SIZE) / 2;

  public static final int SEARCH_TEXT_X = SEARCH_ICON_X + SEARCH_ICON_SIZE + 3;

  public static final int SEARCH_TEXT_Y = SEARCH_Y + (SEARCH_HEIGHT - 8) / 2;

  public static final int SEARCH_TEXT_WIDTH = SEARCH_X + SEARCH_WIDTH - 4 - SEARCH_TEXT_X;

  public static final int SEARCH_TEXT_HEIGHT = 9;

  public static final int LABEL_Y = GRID_Y + GRID_HEIGHT + 5;

  public static final int INVENTORY_X = GRID_X + SLOT_ITEM_OFFSET;

  public static final int MAIN_INVENTORY_Y = LABEL_Y + 11 + SLOT_ITEM_OFFSET;

  public static final int HOTBAR_GAP = 4;

  public static final int HOTBAR_Y = MAIN_INVENTORY_Y + MAIN_ROWS * SLOT_SIZE + HOTBAR_GAP;

  public static final int INVENTORY_LABEL_X = GRID_X;
  public static final int INVENTORY_LABEL_Y = LABEL_Y;

  public static final int STATUS_X = GRID_X;
  public static final int STATUS_Y = LABEL_Y;
  public static final int STATUS_WIDTH = SEARCH_WIDTH;
  public static final int STATUS_HEIGHT = 9;

  public static final int BOTTOM_Y = HOTBAR_Y - SLOT_ITEM_OFFSET + SLOT_SIZE;

  private TerminalScreenLayout() {}

  public static boolean isHotbar(int handlerIndex) {
    return handlerIndex >= HOTBAR_START;
  }

  public static int inventoryIndex(int handlerIndex) {
    requireSlot(handlerIndex);
    return isHotbar(handlerIndex) ? handlerIndex - HOTBAR_START : handlerIndex + COLUMNS;
  }

  public static int x(int handlerIndex) {
    requireSlot(handlerIndex);
    return INVENTORY_X + column(handlerIndex) * SLOT_SIZE;
  }

  public static int y(int handlerIndex) {
    requireSlot(handlerIndex);
    return isHotbar(handlerIndex)
        ? HOTBAR_Y
        : MAIN_INVENTORY_Y + handlerIndex / COLUMNS * SLOT_SIZE;
  }

  public static int cellX(int cell) {
    requireCell(cell);
    return GRID_X + cell % LISTING_COLUMNS * SLOT_SIZE;
  }

  public static int cellY(int cell) {
    requireCell(cell);
    return GRID_Y + cell / LISTING_COLUMNS * SLOT_SIZE;
  }

  public static int cellAt(int windowX, int windowY) {
    final int column = Math.floorDiv(windowX - GRID_X, SLOT_SIZE);
    final int row = Math.floorDiv(windowY - GRID_Y, SLOT_SIZE);
    if (windowX < GRID_X || windowY < GRID_Y || column >= LISTING_COLUMNS || row >= LISTING_ROWS) {
      return NO_CELL;
    }
    return row * LISTING_COLUMNS + column;
  }

  public static boolean isOverSearch(int windowX, int windowY) {
    return windowX >= SEARCH_X
        && windowX < SEARCH_X + SEARCH_WIDTH
        && windowY >= SEARCH_Y
        && windowY < SEARCH_Y + SEARCH_HEIGHT;
  }

  public static boolean isOverScrollbar(int windowX, int windowY) {
    return windowX >= SCROLLBAR_X
        && windowX < SCROLLBAR_X + SCROLLBAR_WIDTH
        && windowY >= SCROLLBAR_Y
        && windowY < SCROLLBAR_Y + SCROLLBAR_HEIGHT;
  }

  public static boolean isOverListing(int windowX, int windowY) {
    return windowX >= GRID_X
        && windowX < SCROLLBAR_X + SCROLLBAR_WIDTH
        && windowY >= GRID_Y
        && windowY < GRID_Y + GRID_HEIGHT;
  }

  public static boolean isOverStatus(int windowX, int windowY) {
    return windowX >= STATUS_X
        && windowX < STATUS_X + STATUS_WIDTH
        && windowY >= STATUS_Y - 1
        && windowY < STATUS_Y + STATUS_HEIGHT;
  }

  private static int column(int handlerIndex) {
    return (isHotbar(handlerIndex) ? handlerIndex - HOTBAR_START : handlerIndex) % COLUMNS;
  }

  private static void requireSlot(int handlerIndex) {
    if (handlerIndex < 0 || handlerIndex >= SLOT_COUNT) {
      throw new IndexOutOfBoundsException("no terminal slot at index " + handlerIndex);
    }
  }

  private static void requireCell(int cell) {
    if (cell < 0 || cell >= LISTING_PAGE_SIZE) {
      throw new IndexOutOfBoundsException("no terminal grid cell at index " + cell);
    }
  }
}
