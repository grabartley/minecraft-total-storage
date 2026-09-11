package com.grahambartley.toomanychests.screenhandler;

public final class TerminalSettingsLayout {
  public static final int NO_ROW = -1;

  public static final int PANEL_X = TerminalScreenLayout.GRID_X;

  public static final int PANEL_Y = TerminalScreenLayout.GRID_Y;

  public static final int PANEL_WIDTH =
      TerminalScreenLayout.GRID_WIDTH
          + TerminalScreenLayout.SCROLLBAR_GAP
          + TerminalScreenLayout.SCROLLBAR_WIDTH;

  public static final int PANEL_HEIGHT = TerminalScreenLayout.GRID_HEIGHT;

  public static final int SECTION_COUNT = 3;

  public static final int SECTION_TAB_HEIGHT = 14;

  public static final int SECTION_GAP = 2;

  public static final int SECTION_TAB_WIDTH = PANEL_WIDTH / SECTION_COUNT;

  public static final int CONTENT_Y = PANEL_Y + SECTION_TAB_HEIGHT + SECTION_GAP;

  public static final int CONTENT_HEIGHT = PANEL_HEIGHT - SECTION_TAB_HEIGHT - SECTION_GAP;

  public static final int ROW_HEIGHT = TerminalScreenLayout.SLOT_SIZE;

  public static final int ROWS = CONTENT_HEIGHT / ROW_HEIGHT;

  public static final int ROW_WIDTH = TerminalScreenLayout.GRID_WIDTH;

  public static final int FULL_ROW_WIDTH = PANEL_WIDTH;

  public static final int CONTROL_SIZE = 16;

  public static final int CONTROL_INSET = (ROW_HEIGHT - CONTROL_SIZE) / 2;

  private TerminalSettingsLayout() {}

  public static int sectionTabX(int section) {
    requireSection(section);
    return PANEL_X + section * SECTION_TAB_WIDTH;
  }

  public static int sectionTabAt(int windowX, int windowY) {
    if (windowY < PANEL_Y || windowY >= PANEL_Y + SECTION_TAB_HEIGHT) {
      return NO_ROW;
    }
    final int section = Math.floorDiv(windowX - PANEL_X, SECTION_TAB_WIDTH);
    return windowX < PANEL_X || section >= SECTION_COUNT ? NO_ROW : section;
  }

  public static int rowY(int row) {
    requireRow(row);
    return CONTENT_Y + row * ROW_HEIGHT;
  }

  public static int rowAt(int windowX, int windowY) {
    if (windowX < PANEL_X || windowX >= PANEL_X + FULL_ROW_WIDTH) {
      return NO_ROW;
    }
    final int row = Math.floorDiv(windowY - CONTENT_Y, ROW_HEIGHT);
    return windowY < CONTENT_Y || row >= ROWS ? NO_ROW : row;
  }

  public static boolean isOverScrollbar(int windowX, int windowY) {
    return windowX >= TerminalScreenLayout.SCROLLBAR_X
        && windowX < TerminalScreenLayout.SCROLLBAR_X + TerminalScreenLayout.SCROLLBAR_WIDTH
        && windowY >= CONTENT_Y
        && windowY < CONTENT_Y + CONTENT_HEIGHT;
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
