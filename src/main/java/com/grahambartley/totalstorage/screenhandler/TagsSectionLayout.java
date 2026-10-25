package com.grahambartley.totalstorage.screenhandler;

public final class TagsSectionLayout {
  public static final int ROW_HEIGHT = 16;

  public static final int TAG_ROWS = 4;

  public static final int LIST_HEIGHT = TAG_ROWS * ROW_HEIGHT;

  public static final int CONTROL_SIZE = 12;

  public static final int CONTROL_GAP = 2;

  public static final int CONTROL_MARGIN = 2;

  public static final int DELETE_X =
      TerminalSettingsLayout.LIST_X
          + TerminalSettingsLayout.LIST_WIDTH
          - CONTROL_MARGIN
          - CONTROL_SIZE;

  public static final int RENAME_X = DELETE_X - CONTROL_GAP - CONTROL_SIZE;

  public static final int ARMED_WIDTH = 46;

  public static final int ARMED_HEIGHT = 14;

  public static final int ARMED_X =
      TerminalSettingsLayout.LIST_X
          + TerminalSettingsLayout.LIST_WIDTH
          - CONTROL_MARGIN
          - ARMED_WIDTH;

  public static final int TEXT_MARGIN = 3;

  public static final int COUNT_GAP = 6;

  public static final int TEXT_HEIGHT = 8;

  public static final int FOOTER_Y =
      TerminalSettingsLayout.LIST_Y
          + LIST_HEIGHT
          + (TerminalSettingsLayout.FRAME_Y
                  + TerminalSettingsLayout.FRAME_HEIGHT
                  - 1
                  - TerminalSettingsLayout.LIST_Y
                  - LIST_HEIGHT
                  - TEXT_HEIGHT)
              / 2;

  private TagsSectionLayout() {}

  public static int rowY(int row) {
    if (row < 0 || row >= TAG_ROWS) {
      throw new IndexOutOfBoundsException("no tag row at " + row);
    }
    return TerminalSettingsLayout.LIST_Y + row * ROW_HEIGHT;
  }

  public static int rowAt(int windowX, int windowY) {
    if (windowX < TerminalSettingsLayout.LIST_X
        || windowX >= TerminalSettingsLayout.LIST_X + TerminalSettingsLayout.LIST_WIDTH) {
      return TerminalSettingsLayout.NO_ROW;
    }
    final int row = Math.floorDiv(windowY - TerminalSettingsLayout.LIST_Y, ROW_HEIGHT);
    return windowY < TerminalSettingsLayout.LIST_Y || row >= TAG_ROWS
        ? TerminalSettingsLayout.NO_ROW
        : row;
  }

  public static int controlY(int row) {
    return rowY(row) + (ROW_HEIGHT - CONTROL_SIZE) / 2;
  }

  public static int armedY(int row) {
    return rowY(row) + (ROW_HEIGHT - ARMED_HEIGHT) / 2;
  }

  public static TagRowRegion regionAt(int windowX, int windowY, boolean armed) {
    final int row = rowAt(windowX, windowY);
    if (row == TerminalSettingsLayout.NO_ROW) {
      return TagRowRegion.NONE;
    }
    if (armed) {
      return isInside(windowX, windowY, ARMED_X, armedY(row), ARMED_WIDTH, ARMED_HEIGHT)
          ? TagRowRegion.DELETE
          : TagRowRegion.NAME;
    }
    if (isInside(windowX, windowY, DELETE_X, controlY(row), CONTROL_SIZE, CONTROL_SIZE)) {
      return TagRowRegion.DELETE;
    }
    if (isInside(windowX, windowY, RENAME_X, controlY(row), CONTROL_SIZE, CONTROL_SIZE)) {
      return TagRowRegion.RENAME;
    }
    return TagRowRegion.NAME;
  }

  public static int tagIndexAt(int windowX, int windowY, int firstVisible) {
    final int row = rowAt(windowX, windowY);
    return row == TerminalSettingsLayout.NO_ROW
        ? TerminalSettingsLayout.NO_ROW
        : firstVisible + row;
  }

  private static boolean isInside(
      int windowX, int windowY, int left, int top, int width, int height) {
    return windowX >= left && windowX < left + width && windowY >= top && windowY < top + height;
  }
}
