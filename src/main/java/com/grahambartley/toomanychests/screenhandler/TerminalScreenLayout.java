package com.grahambartley.toomanychests.screenhandler;

/**
 * Where everything sits in the terminal window, in the window's own coordinates.
 *
 * <p>The handler and the screen have to agree on this exactly: the handler decides where a slot is
 * clickable and the screen decides where it is drawn, and a disagreement between them is a slot a
 * player can see but not use. Holding both in one place, as plain arithmetic over plain constants,
 * is what lets that agreement be tested without a running game.
 *
 * <p>The window is wider than a chest because the item grid and the filter column have to live
 * above the player inventory. The inventory is centred rather than left-aligned, since a vanilla
 * screen's eight pixel margin only reads as centred at vanilla's width.
 */
public final class TerminalScreenLayout {
  public static final int WINDOW_WIDTH = 230;

  /**
   * How thick the chassis sprite's fixed border is. Anything drawn under it is drawn behind the
   * frame, so the slots have to clear it rather than merely fit inside the window.
   */
  public static final int PANEL_BORDER = 6;

  public static final int COLUMNS = 9;
  public static final int MAIN_ROWS = 3;
  public static final int SLOT_SIZE = 18;

  /** Handler slot indices: the main inventory first, then the hotbar. */
  public static final int MAIN_SLOT_COUNT = MAIN_ROWS * COLUMNS;

  public static final int HOTBAR_START = MAIN_SLOT_COUNT;
  public static final int SLOT_COUNT = HOTBAR_START + COLUMNS;

  public static final int TITLE_X = 8;
  public static final int TITLE_Y = PANEL_BORDER + 2;

  public static final int INVENTORY_X = (WINDOW_WIDTH - COLUMNS * SLOT_SIZE) / 2;
  public static final int MAIN_INVENTORY_Y = 140;

  /** The four pixel gap between the main inventory and the hotbar, as vanilla draws it. */
  public static final int HOTBAR_GAP = 4;

  public static final int HOTBAR_Y = MAIN_INVENTORY_Y + MAIN_ROWS * SLOT_SIZE + HOTBAR_GAP;

  public static final int WINDOW_HEIGHT = HOTBAR_Y + SLOT_SIZE + PANEL_BORDER + HOTBAR_GAP;

  /** The recessed area the item grid and the filter column will be drawn into. */
  public static final int INSET_X = PANEL_BORDER + 1;

  public static final int INSET_Y = TITLE_Y + 10;
  public static final int INSET_WIDTH = WINDOW_WIDTH - 2 * INSET_X;
  public static final int INSET_HEIGHT = 108;

  /**
   * How many cells the listing recess holds. A page is asked for one recess at a time, so what the
   * client requests is set by the space it has to draw into rather than by how much the network
   * holds.
   */
  public static final int LISTING_COLUMNS = INSET_WIDTH / SLOT_SIZE;

  public static final int LISTING_ROWS = INSET_HEIGHT / SLOT_SIZE;
  public static final int LISTING_PAGE_SIZE = LISTING_COLUMNS * LISTING_ROWS;

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

  private static int column(int handlerIndex) {
    return (isHotbar(handlerIndex) ? handlerIndex - HOTBAR_START : handlerIndex) % COLUMNS;
  }

  private static void requireSlot(int handlerIndex) {
    if (handlerIndex < 0 || handlerIndex >= SLOT_COUNT) {
      throw new IndexOutOfBoundsException("no terminal slot at index " + handlerIndex);
    }
  }
}
