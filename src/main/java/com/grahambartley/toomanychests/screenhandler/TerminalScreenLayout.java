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
 * only reads as centred at vanilla's width. It is centred under the grid rather than under the
 * recess or the window, both of which are wider than the grid by chrome rather than by content.
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

  public static final int SEARCH_HEIGHT = 12;

  /** A toolbar button, sized so a 12x12 icon centres inside it with a border to spare. */
  public static final int BUTTON_SIZE = 20;

  /** Every toolbar icon is this size, per {@code docs/terminal-gui-sprites.md}. */
  public static final int ICON_SIZE = 12;

  /** Where an icon sits inside its button, which is what centres it. */
  public static final int ICON_INSET = (BUTTON_SIZE - ICON_SIZE) / 2;

  /** Air between the recess and the button column beside it, so the two read as separate panels. */
  public static final int TOOLBAR_GAP = 2;

  /** How far the search box's own nine-sliced border reaches, which its text has to clear. */
  public static final int SEARCH_BORDER = 4;

  /** Air between the search box and the recess, so the two read as separate panels. */
  public static final int SEARCH_GAP = 2;

  /**
   * The air above and below the line of text inside the box, which centres it there.
   *
   * <p>A line of text is eight pixels tall and the box is twelve, so two pixels centres it. That is
   * thinner than the sprite's nine-slice border, which is deliberate: the border a player can see
   * is the outer two pixels of it, and clearing the whole slice would leave no room for the line.
   */
  public static final int SEARCH_TEXT_MARGIN = 2;

  /** The recessed area the item grid, its scrollbar, and later the filter column are drawn into. */
  public static final int INSET_X = PANEL_BORDER + 1;

  public static final int SEARCH_X = INSET_X;
  public static final int SEARCH_Y = TITLE_Y + 10;

  public static final int INSET_Y = SEARCH_Y + SEARCH_HEIGHT + SEARCH_GAP;

  public static final int INSET_WIDTH =
      2 * INSET_BORDER + GRID_WIDTH + SCROLLBAR_GAP + SCROLLBAR_WIDTH;

  public static final int INSET_HEIGHT = 2 * INSET_BORDER + GRID_HEIGHT;

  /**
   * The column of actions that operate on the whole network, beside the recess rather than above
   * it. The window is already as tall as vanilla will draw one at the smallest screen size, so a
   * row would have cost the grid a line of cells; per {@code docs/terminal-gui-sprites.md} a
   * control is given room by widening the window instead.
   */
  public static final int TOOLBAR_X = INSET_X + INSET_WIDTH + TOOLBAR_GAP;

  public static final int TOOLBAR_Y = INSET_Y;

  /**
   * How many views the terminal offers, each with a tab at the top of the toolbar column.
   *
   * <p>The tabs live in the column rather than above the window, where vanilla puts them, because
   * the window is already as tall as vanilla will draw one on a 320x240 screen. A strip above it
   * would be drawn off the bottom of that screen instead.
   */
  public static final int TAB_COUNT = 2;

  /** Air between the tab strip and the actions under it, so the two do not read as one column. */
  public static final int TOOLBAR_DIVIDER = 4;

  public static final int ACTIONS_Y = TOOLBAR_Y + TAB_COUNT * BUTTON_SIZE + TOOLBAR_DIVIDER;

  public static final int WINDOW_WIDTH = TOOLBAR_X + BUTTON_SIZE + INSET_X;

  /** Where a tab sits in the strip, counted from the top of the toolbar column. */
  public static int tabY(int index) {
    requireTab(index);
    return TOOLBAR_Y + index * BUTTON_SIZE;
  }

  /** Where an action button sits, counted from the top of the actions below the tabs. */
  public static int actionY(int index) {
    if (index < 0
        || ACTIONS_Y + (index + 1) * (BUTTON_SIZE + TOOLBAR_GAP) > INSET_Y + INSET_HEIGHT) {
      throw new IndexOutOfBoundsException("no room in the toolbar for an action at " + index);
    }
    return ACTIONS_Y + index * (BUTTON_SIZE + TOOLBAR_GAP);
  }

  /** Which tab a point in the window is over, or {@link #NO_CELL} when it is over none. */
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

  /** The search box spans the recess under it, since it narrows everything the recess shows. */
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

  /**
   * The inventory is centred under the grid, rather than under the recess or the window.
   *
   * <p>Both of those are wider than the grid by things the eye does not read as content: the recess
   * carries the scrollbar and the window carries the toolbar column. Centring on either leaves the
   * inventory visibly off to one side of the nine columns directly above it, which are the nine
   * columns a player is comparing it against.
   */
  public static final int INVENTORY_X = GRID_X + (GRID_WIDTH - COLUMNS * SLOT_SIZE) / 2;

  /** Room under the recess for the inventory's own label, plus air on either side of it. */
  public static final int INVENTORY_GAP = 14;

  public static final int MAIN_INVENTORY_Y = INSET_Y + INSET_HEIGHT + INVENTORY_GAP;

  /** The four pixel gap between the main inventory and the hotbar, as vanilla draws it. */
  public static final int HOTBAR_GAP = 4;

  public static final int HOTBAR_Y = MAIN_INVENTORY_Y + MAIN_ROWS * SLOT_SIZE + HOTBAR_GAP;

  /** Air under the hotbar, so its stack counts do not sit against the chassis frame. */
  public static final int BOTTOM_MARGIN = 4;

  public static final int WINDOW_HEIGHT = HOTBAR_Y + SLOT_SIZE + BOTTOM_MARGIN + PANEL_BORDER;

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

  /**
   * Whether a point in the window is over the search box, border and all.
   *
   * <p>The whole box rather than the text inside it, because the field covers only the text and a
   * click on the border of something that looks like one control should reach it.
   */
  public static boolean isOverSearch(int windowX, int windowY) {
    return windowX >= SEARCH_X
        && windowX < SEARCH_X + SEARCH_WIDTH
        && windowY >= SEARCH_Y
        && windowY < SEARCH_Y + SEARCH_HEIGHT;
  }

  /** Whether a point in the window is over the scrollbar track. */
  public static boolean isOverScrollbar(int windowX, int windowY) {
    return windowX >= SCROLLBAR_X
        && windowX < SCROLLBAR_X + SCROLLBAR_WIDTH
        && windowY >= SCROLLBAR_Y
        && windowY < SCROLLBAR_Y + SCROLLBAR_HEIGHT;
  }

  /**
   * Whether a point in the window is over the listing, meaning the grid, the scrollbar, or the gap
   * between them.
   *
   * <p>One rectangle rather than the union of the two, so the gap does not become a strip the wheel
   * is ignored over. A player aiming at a scrollbar two pixels away from a grid is aiming at the
   * listing either way.
   */
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
