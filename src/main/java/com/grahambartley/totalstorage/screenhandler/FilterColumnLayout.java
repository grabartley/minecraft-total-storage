package com.grahambartley.totalstorage.screenhandler;

public final class FilterColumnLayout {
  public static final int NO_ROW = -1;

  public static final int X = TerminalScreenLayout.SIDE_X;

  public static final int Y = TerminalScreenLayout.HEADER_Y;

  public static final int WIDTH = TerminalScreenLayout.SIDE_WIDTH;

  public static final int HEADER_HEIGHT = TerminalScreenLayout.HEADER_HEIGHT;

  public static final int HEADER_GAP = 2;

  public static final int CONTROL_SIZE = HEADER_HEIGHT;

  public static final int CLEAR_X = X + WIDTH - CONTROL_SIZE;

  public static final int MODE_WIDTH = WIDTH - CONTROL_SIZE - HEADER_GAP;

  public static final int FRAME_Y = TerminalScreenLayout.GRID_Y;

  public static final int FRAME_HEIGHT = TerminalScreenLayout.GRID_HEIGHT;

  public static final int HEIGHT = FRAME_Y + FRAME_HEIGHT - Y;

  public static final int CONTENT_Y = FRAME_Y + 1;

  public static final int CONTENT_HEIGHT = FRAME_HEIGHT - 2;

  public static final int ROW_HEIGHT = 11;

  public static final int ROWS = CONTENT_HEIGHT / ROW_HEIGHT;

  public static final int ROW_X = X + 1;

  public static final int ROW_WIDTH = 76;

  public static final int SCROLLBAR_WIDTH = 5;

  public static final int SCROLLBAR_X = X + WIDTH - 1 - SCROLLBAR_WIDTH;

  public static final int TEXT_MARGIN = 3;

  public static final int CHECK_SIZE = 6;

  public static final int CHECK_GAP = 2;

  public static final int COUNT_GAP = 4;

  private FilterColumnLayout() {}

  public static int nameLeft(boolean checked) {
    return TEXT_MARGIN + (checked ? CHECK_SIZE + CHECK_GAP : 0);
  }

  public static int nameWidth(int countWidth, boolean checked) {
    return Math.max(
        0, ROW_WIDTH - nameLeft(checked) - TEXT_MARGIN - Math.max(0, countWidth) - COUNT_GAP);
  }

  public static int countLeft(int countWidth) {
    return ROW_WIDTH - TEXT_MARGIN - Math.max(0, countWidth);
  }

  public static int rowY(int row) {
    requireRow(row);
    return CONTENT_Y + row * ROW_HEIGHT;
  }

  public static int rowAt(int windowX, int windowY) {
    if (windowX < ROW_X || windowX >= ROW_X + ROW_WIDTH) {
      return NO_ROW;
    }
    final int row = Math.floorDiv(windowY - CONTENT_Y, ROW_HEIGHT);
    return windowY < CONTENT_Y || row >= ROWS ? NO_ROW : row;
  }

  public static boolean isOverColumn(int windowX, int windowY) {
    return windowX >= X && windowX < X + WIDTH && windowY >= Y && windowY < Y + HEIGHT;
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
