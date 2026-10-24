package com.grahambartley.totalstorage.screenhandler;

public final class TerminalScreenLayout {

  public static final int PANEL_BORDER = 6;

  public static final int INSET_BORDER = 4;

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

  public static final int SCROLLBAR_WIDTH = 8;

  public static final int SCROLLBAR_GAP = 2;

  public static final int MAIN_SLOT_COUNT = MAIN_ROWS * COLUMNS;

  public static final int HOTBAR_START = MAIN_SLOT_COUNT;
  public static final int SLOT_COUNT = HOTBAR_START + COLUMNS;

  public static final int TITLE_X = 8;
  public static final int TITLE_Y = PANEL_BORDER + 2;

  public static final int SEARCH_HEIGHT = 12;

  public static final int BUTTON_SIZE = 20;

  public static final int ICON_SIZE = 12;

  public static final int ICON_INSET = (BUTTON_SIZE - ICON_SIZE) / 2;

  public static final int TOOLBAR_GAP = 2;

  public static final int SEARCH_BORDER = 4;

  public static final int SEARCH_GAP = 2;

  public static final int SEARCH_TEXT_MARGIN = 2;

  public static final int EDGE = PANEL_BORDER + 1;

  public static final int FILTER_WIDTH = 76;

  public static final int FILTER_GAP = 3;

  public static final int FILTER_X = EDGE;

  public static final int INSET_X = FILTER_X + FILTER_WIDTH + FILTER_GAP;

  public static final int SEARCH_X = INSET_X;
  public static final int SEARCH_Y = TITLE_Y + 10;

  public static final int INSET_Y = SEARCH_Y + SEARCH_HEIGHT + SEARCH_GAP;

  public static final int INSET_WIDTH =
      2 * INSET_BORDER + GRID_WIDTH + SCROLLBAR_GAP + SCROLLBAR_WIDTH;

  public static final int INSET_HEIGHT = 2 * INSET_BORDER + GRID_HEIGHT;

  public static final int TOOLBAR_X = INSET_X + INSET_WIDTH + TOOLBAR_GAP;

  public static final int TOOLBAR_Y = SEARCH_Y;

  public static final int TAB_COUNT = 2;

  public static final int TOOLBAR_DIVIDER = 4;

  public static final int ACTIONS_Y = TOOLBAR_Y + TAB_COUNT * BUTTON_SIZE + TOOLBAR_DIVIDER;

  public static final int WINDOW_WIDTH = TOOLBAR_X + BUTTON_SIZE + EDGE;

  public static int tabY(int index) {
    requireTab(index);
    return TOOLBAR_Y + index * BUTTON_SIZE;
  }

  public static int actionY(int index) {
    if (index < 0
        || ACTIONS_Y + (index + 1) * (BUTTON_SIZE + TOOLBAR_GAP) > INSET_Y + INSET_HEIGHT) {
      throw new IndexOutOfBoundsException("no room in the toolbar for an action at " + index);
    }
    return ACTIONS_Y + index * (BUTTON_SIZE + TOOLBAR_GAP);
  }

  public static int tabAt(int windowX, int windowY) {
    if (windowX < TOOLBAR_X || windowX >= TOOLBAR_X + BUTTON_SIZE) {
      return NO_CELL;
    }
    final int index = Math.floorDiv(windowY - TOOLBAR_Y, BUTTON_SIZE);
    return windowY < TOOLBAR_Y || index >= TAB_COUNT ? NO_CELL : index;
  }

  private static void requireTab(int index) {
    if (index < 0 || index >= TAB_COUNT) {
      throw new IndexOutOfBoundsException("no terminal tab at index " + index);
    }
  }

  public static final int SEARCH_WIDTH = INSET_WIDTH;

  public static final int SEARCH_TEXT_X = SEARCH_X + SEARCH_BORDER;

  public static final int SEARCH_TEXT_HEIGHT = SEARCH_HEIGHT - 2 * SEARCH_TEXT_MARGIN;

  public static final int SEARCH_TEXT_Y = SEARCH_Y + SEARCH_TEXT_MARGIN;

  public static final int SEARCH_TEXT_WIDTH = SEARCH_WIDTH - 2 * SEARCH_BORDER;

  public static final int GRID_X = INSET_X + INSET_BORDER;
  public static final int GRID_Y = INSET_Y + INSET_BORDER;

  public static final int SCROLLBAR_X = GRID_X + GRID_WIDTH + SCROLLBAR_GAP;
  public static final int SCROLLBAR_Y = GRID_Y;
  public static final int SCROLLBAR_HEIGHT = GRID_HEIGHT;

  public static final int INVENTORY_X = GRID_X + SLOT_ITEM_OFFSET;

  public static final int INVENTORY_GAP = 14;

  public static final int MAIN_INVENTORY_Y = INSET_Y + INSET_HEIGHT + INVENTORY_GAP;

  public static final int HOTBAR_GAP = 4;

  public static final int HOTBAR_Y = MAIN_INVENTORY_Y + MAIN_ROWS * SLOT_SIZE + HOTBAR_GAP;

  public static final int BOTTOM_MARGIN = 4;

  public static final int WINDOW_HEIGHT = HOTBAR_Y + SLOT_SIZE + BOTTOM_MARGIN + PANEL_BORDER;

  public static final int INVENTORY_LABEL_X = INVENTORY_X;
  public static final int INVENTORY_LABEL_Y = MAIN_INVENTORY_Y - 11;

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

  public static final int NOTICE_PADDING = 3;

  public static final int NOTICE_LINE_HEIGHT = 9;

  public static final int NOTICE_MAX_LINES = 3;

  public static final int NOTICE_X = GRID_X;

  public static final int NOTICE_Y = GRID_Y;

  public static final int NOTICE_WIDTH = GRID_WIDTH + SCROLLBAR_GAP + SCROLLBAR_WIDTH;

  public static final int NOTICE_TEXT_WIDTH = NOTICE_WIDTH - 2 * NOTICE_PADDING;

  public static int pageCellAt(int screenCell, int hiddenRows) {
    if (screenCell == NO_CELL) {
      return NO_CELL;
    }
    final int covered = Math.max(0, hiddenRows) * LISTING_COLUMNS;
    return screenCell < covered ? NO_CELL : screenCell - covered;
  }

  public static int screenCellFor(int pageCell, int hiddenRows) {
    if (pageCell == NO_CELL) {
      return NO_CELL;
    }
    final int cell = pageCell + Math.max(0, hiddenRows) * LISTING_COLUMNS;
    return cell >= LISTING_PAGE_SIZE ? NO_CELL : cell;
  }

  public static int noticeRows(int lines) {
    if (lines < 1 || lines > NOTICE_MAX_LINES) {
      throw new IllegalArgumentException("a notice cannot be " + lines + " lines tall");
    }
    final int text = 2 * NOTICE_PADDING + lines * NOTICE_LINE_HEIGHT;
    return Math.min(LISTING_ROWS, (text + SLOT_SIZE - 1) / SLOT_SIZE);
  }

  public static int noticeHeight(int lines) {
    return noticeRows(lines) * SLOT_SIZE;
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

  public static int filterBottom() {
    return INSET_Y + INSET_HEIGHT;
  }

  public static boolean isOverListing(int windowX, int windowY) {
    return windowX >= GRID_X
        && windowX < SCROLLBAR_X + SCROLLBAR_WIDTH
        && windowY >= GRID_Y
        && windowY < GRID_Y + GRID_HEIGHT;
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
