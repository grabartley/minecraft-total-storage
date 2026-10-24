package com.grahambartley.totalstorage.screenhandler;

public final class FilterColumnLayout {
  public static final int NO_ROW = -1;

  public static final int X = TerminalScreenLayout.FILTER_X;

  public static final int Y = TerminalScreenLayout.SEARCH_Y;

  public static final int WIDTH = TerminalScreenLayout.FILTER_WIDTH;

  public static final int HEIGHT = TerminalScreenLayout.filterBottom() - Y;

  public static final int HEADER_HEIGHT = TerminalScreenLayout.SEARCH_HEIGHT;

  public static final int HEADER_GAP = 2;

  public static final int CONTROL_SIZE = 12;

  public static final int CLEAR_X = X + WIDTH - CONTROL_SIZE;

  public static final int MODE_WIDTH = WIDTH - CONTROL_SIZE - HEADER_GAP;

  public static final int CONTENT_Y = Y + HEADER_HEIGHT + HEADER_GAP;

  public static final int CONTENT_HEIGHT = HEIGHT - HEADER_HEIGHT - HEADER_GAP;

  public static final int ROW_HEIGHT = 12;

  public static final int ROWS = CONTENT_HEIGHT / ROW_HEIGHT;

  public static final int SCROLLBAR_WIDTH = 4;

  public static final int SCROLLBAR_X = X + WIDTH - SCROLLBAR_WIDTH;

  public static final int SCROLLBAR_GAP = 1;

  public static final int ROW_WIDTH = WIDTH - SCROLLBAR_WIDTH - SCROLLBAR_GAP;

  public static final int TEXT_MARGIN = 4;

  public static final int COUNT_GAP = 3;

  private FilterColumnLayout() {}

  public static int nameWidth(int countWidth) {
    return Math.max(0, ROW_WIDTH - 2 * TEXT_MARGIN - Math.max(0, countWidth) - COUNT_GAP);
  }

  public static int countLeft(int countWidth) {
    return ROW_WIDTH - TEXT_MARGIN - Math.max(0, countWidth);
  }

  public static int rowY(int row) {
    requireRow(row);
    return CONTENT_Y + row * ROW_HEIGHT;
  }

  public static int rowAt(int windowX, int windowY) {
    if (windowX < X || windowX >= X + ROW_WIDTH) {
      return NO_ROW;
    }
    final int row = Math.floorDiv(windowY - CONTENT_Y, ROW_HEIGHT);
    return windowY < CONTENT_Y || row >= ROWS ? NO_ROW : row;
  }

  public static boolean isOverColumn(int windowX, int windowY) {
    return windowX >= X && windowX < X + WIDTH && windowY >= Y && windowY < Y + HEIGHT;
  }

  public static boolean isOverMode(int windowX, int windowY) {
    return windowX >= X && windowX < X + MODE_WIDTH && windowY >= Y && windowY < Y + HEADER_HEIGHT;
  }

  public static boolean isOverClear(int windowX, int windowY) {
    return windowX >= CLEAR_X
        && windowX < CLEAR_X + CONTROL_SIZE
        && windowY >= Y
        && windowY < Y + CONTROL_SIZE;
  }

  public static boolean isOverScrollbar(int windowX, int windowY) {
    return windowX >= SCROLLBAR_X
        && windowX < SCROLLBAR_X + SCROLLBAR_WIDTH
        && windowY >= CONTENT_Y
        && windowY < CONTENT_Y + CONTENT_HEIGHT;
  }

  private static void requireRow(int row) {
    if (row < 0 || row >= ROWS) {
      throw new IndexOutOfBoundsException("no filter row at " + row);
    }
  }
}
