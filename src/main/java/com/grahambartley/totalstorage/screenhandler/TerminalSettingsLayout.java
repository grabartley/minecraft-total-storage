package com.grahambartley.totalstorage.screenhandler;

public final class TerminalSettingsLayout {
  public static final int NO_ROW = -1;

  public static final int FRAME_X = TerminalScreenLayout.EDGE;

  public static final int FRAME_Y = TerminalScreenLayout.GRID_Y - 2;

  public static final int FRAME_WIDTH =
      TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH - FRAME_X;

  public static final int FRAME_HEIGHT =
      TerminalScreenLayout.GRID_Y + TerminalScreenLayout.GRID_HEIGHT - FRAME_Y;

  public static final int SECTION_COUNT = 3;

  public static final int SECTION_TAB_WIDTH = FRAME_WIDTH / SECTION_COUNT;

  public static final int SECTION_TAB_Y = TerminalScreenLayout.HEADER_Y + 2;

  public static final int SECTION_TAB_HEIGHT = FRAME_Y + 1 - SECTION_TAB_Y;

  public static final int SELECTED_TAB_Y = TerminalScreenLayout.HEADER_Y;

  public static final int SELECTED_TAB_HEIGHT = FRAME_Y + 2 - SELECTED_TAB_Y;

  public static final int PADDING = 9;

  public static final int CONTENT_X = FRAME_X + PADDING;

  public static final int CONTENT_RIGHT = FRAME_X + FRAME_WIDTH - PADDING;

  public static final int LIST_X = FRAME_X + 4;

  public static final int LIST_Y = FRAME_Y + 4;

  public static final int SCROLLBAR_WIDTH = 6;

  public static final int SCROLLBAR_X = FRAME_X + FRAME_WIDTH - 5 - SCROLLBAR_WIDTH;

  public static final int LIST_WIDTH = SCROLLBAR_X - 4 - LIST_X;

  public static final int ROW_HEIGHT = 21;

  public static final int ROWS = 4;

  public static final int LIST_HEIGHT = ROWS * ROW_HEIGHT;

  public static final int BUTTON_WIDTH = 100;

  public static final int BUTTON_HEIGHT = 20;

  public static final int BUTTON_X = CONTENT_RIGHT - BUTTON_WIDTH;

  public static final int SETTING_GAP = 6;

  public static final int SETTING_COUNT = 3;

  public static final int SETTINGS_Y =
      FRAME_Y
          + 1
          + (FRAME_HEIGHT - 2 - SETTING_COUNT * BUTTON_HEIGHT - (SETTING_COUNT - 1) * SETTING_GAP)
              / 2;

  public static final int DESCRIPTION_WIDTH = BUTTON_X - 6 - CONTENT_X;

  private TerminalSettingsLayout() {}

  public static int settingY(int row) {
    if (row < 0 || row >= SETTING_COUNT) {
      throw new IndexOutOfBoundsException("no terminal setting at row " + row);
    }
    return SETTINGS_Y + row * (BUTTON_HEIGHT + SETTING_GAP);
  }

  public static int sectionTabX(int section) {
    requireSection(section);
    return FRAME_X + section * SECTION_TAB_WIDTH;
  }

  public static int sectionTabAt(int windowX, int windowY) {
    if (windowY < SELECTED_TAB_Y || windowY >= FRAME_Y) {
      return NO_ROW;
    }
    final int section = Math.floorDiv(windowX - FRAME_X, SECTION_TAB_WIDTH);
    return windowX < FRAME_X || section >= SECTION_COUNT ? NO_ROW : section;
  }

  public static int rowY(int row) {
    requireRow(row);
    return LIST_Y + row * ROW_HEIGHT;
  }

  public static int rowAt(int windowX, int windowY) {
    if (windowX < LIST_X || windowX >= LIST_X + LIST_WIDTH) {
      return NO_ROW;
    }
    final int row = Math.floorDiv(windowY - LIST_Y, ROW_HEIGHT);
    return windowY < LIST_Y || row >= ROWS ? NO_ROW : row;
  }

  private static void requireSection(int section) {
    if (section < 0 || section >= SECTION_COUNT) {
      throw new IndexOutOfBoundsException("no settings section at " + section);
    }
  }

  private static void requireRow(int row) {
    if (row < 0 || row >= ROWS) {
      throw new IndexOutOfBoundsException("no settings row at " + row);
    }
  }
}
