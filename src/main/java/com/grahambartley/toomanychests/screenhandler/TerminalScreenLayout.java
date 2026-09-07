package com.grahambartley.toomanychests.screenhandler;

/**
 * Where everything sits in the terminal window, in the window's own coordinates.
 *
 * <p>The handler and the screen have to agree on this exactly: the handler decides where a slot is
 * clickable and the screen decides where it is drawn, and a disagreement between them is a slot a
 * player can see but not use. Holding both in one place, as plain arithmetic over plain constants,
 * is what lets that agreement be tested without a running game.
 *
 * <p>The window is sized from the grid outwards rather than picked and then divided up, so the
 * recess holds a whole number of cells with no strip of dead space beside them. Per {@code
 * docs/terminal-gui-sprites.md} the filter column is grown by widening the window rather than by
 * narrowing the grid, so the grid keeps its width when that column arrives.
 *
 * <p>The inventory is centred rather than left-aligned, since a vanilla screen's eight pixel margin
 * only reads as centred at vanilla's width.
 */
public final class TerminalScreenLayout {
  /**
   * How thick the chassis sprite's fixed border is. Anything drawn under it is drawn behind the
   * frame, so the slots have to clear it rather than merely fit inside the window.
   */
  public static final int PANEL_BORDER = 6;

  /** How thick the recess sprite's fixed border is, which is what its contents have to clear. */
  public static final int INSET_BORDER = 4;

  public static final int COLUMNS = 9;
  public static final int MAIN_ROWS = 3;
  public static final int SLOT_SIZE = 18;

  /** A grid cell, which is wider than an inventory slot because it carries its own state gutter. */
  public static final int CELL_SIZE = 20;

  /** Where the 16x16 item sits inside a cell, clear of the bevel and the gutter around it. */
  public static final int CELL_ITEM_OFFSET = 2;

  public static final int LISTING_COLUMNS = 9;
  public static final int LISTING_ROWS = 5;
  public static final int LISTING_PAGE_SIZE = LISTING_COLUMNS * LISTING_ROWS;

  /** Returned by {@link #cellAt} for a position that is not over the grid. */
  public static final int NO_CELL = -1;

  public static final int GRID_WIDTH = LISTING_COLUMNS * CELL_SIZE;
  public static final int GRID_HEIGHT = LISTING_ROWS * CELL_SIZE;

  public static final int SCROLLBAR_WIDTH = 8;

  /** The gap between the grid and its scrollbar, so a hovered cell does not touch the track. */
  public static final int SCROLLBAR_GAP = 2;

  /** Handler slot indices: the main inventory first, then the hotbar. */
  public static final int MAIN_SLOT_COUNT = MAIN_ROWS * COLUMNS;

  public static final int HOTBAR_START = MAIN_SLOT_COUNT;
  public static final int SLOT_COUNT = HOTBAR_START + COLUMNS;

  public static final int TITLE_X = 8;
  public static final int TITLE_Y = PANEL_BORDER + 2;

  /** The recessed area the item grid, its scrollbar, and later the filter column are drawn into. */
  public static final int INSET_X = PANEL_BORDER + 1;

  public static final int INSET_Y = TITLE_Y + 10;

  public static final int INSET_WIDTH =
      2 * INSET_BORDER + GRID_WIDTH + SCROLLBAR_GAP + SCROLLBAR_WIDTH;

  public static final int INSET_HEIGHT = 2 * INSET_BORDER + GRID_HEIGHT;

  public static final int WINDOW_WIDTH = INSET_WIDTH + 2 * INSET_X;

  public static final int GRID_X = INSET_X + INSET_BORDER;
  public static final int GRID_Y = INSET_Y + INSET_BORDER;

  public static final int SCROLLBAR_X = GRID_X + GRID_WIDTH + SCROLLBAR_GAP;
  public static final int SCROLLBAR_Y = GRID_Y;
  public static final int SCROLLBAR_HEIGHT = GRID_HEIGHT;

  public static final int INVENTORY_X = (WINDOW_WIDTH - COLUMNS * SLOT_SIZE) / 2;
  public static final int MAIN_INVENTORY_Y = INSET_Y + INSET_HEIGHT + 14;

  /** The four pixel gap between the main inventory and the hotbar, as vanilla draws it. */
  public static final int HOTBAR_GAP = 4;

  public static final int HOTBAR_Y = MAIN_INVENTORY_Y + MAIN_ROWS * SLOT_SIZE + HOTBAR_GAP;

  public static final int WINDOW_HEIGHT = HOTBAR_Y + SLOT_SIZE + PANEL_BORDER + HOTBAR_GAP;

  public static final int INVENTORY_LABEL_X = INVENTORY_X;
  public static final int INVENTORY_LABEL_Y = MAIN_INVENTORY_Y - 11;

  private TerminalScreenLayout() {}

  public static boolean isHotbar(int handlerIndex) {
    return handlerIndex >= HOTBAR_START;
  }

  /** The slot in the player's own inventory that a handler slot index stands for. */
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
    return GRID_X + cell % LISTING_COLUMNS * CELL_SIZE;
  }

  public static int cellY(int cell) {
    requireCell(cell);
    return GRID_Y + cell / LISTING_COLUMNS * CELL_SIZE;
  }

  /**
   * Which cell a point in the window is over, or {@link #NO_CELL} when it is over none.
   *
   * <p>Cells tile with no spacing, so every point inside the grid is over exactly one of them and
   * the hit test is the drawing arithmetic run backwards. Anything outside is a miss rather than
   * the nearest cell, because a click that lands beside the grid did not mean a cell.
   */
  public static int cellAt(int windowX, int windowY) {
    final int column = Math.floorDiv(windowX - GRID_X, CELL_SIZE);
    final int row = Math.floorDiv(windowY - GRID_Y, CELL_SIZE);
    if (windowX < GRID_X || windowY < GRID_Y || column >= LISTING_COLUMNS || row >= LISTING_ROWS) {
      return NO_CELL;
    }
    return row * LISTING_COLUMNS + column;
  }

  /** Whether a point in the window is over the scrollbar track. */
  public static boolean isOverScrollbar(int windowX, int windowY) {
    return windowX >= SCROLLBAR_X
        && windowX < SCROLLBAR_X + SCROLLBAR_WIDTH
        && windowY >= SCROLLBAR_Y
        && windowY < SCROLLBAR_Y + SCROLLBAR_HEIGHT;
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
