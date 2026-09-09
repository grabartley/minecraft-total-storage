package com.grahambartley.toomanychests.screenhandler;

public final class TagsSectionLayout {
  public static final int NEW_TAG_ROW = 0;

  public static final int FIRST_TAG_ROW = 1;

  public static final int TAG_ROWS = TerminalSettingsLayout.ROWS - FIRST_TAG_ROW;

  public static final int CONTROL_SIZE = 12;

  public static final int CONTROL_MARGIN = 2;

  public static final int DELETE_X =
      TerminalSettingsLayout.PANEL_X
          + TerminalSettingsLayout.ROW_WIDTH
          - CONTROL_SIZE
          - CONTROL_MARGIN;

  public static final int CONTROL_INSET = (TerminalSettingsLayout.ROW_HEIGHT - CONTROL_SIZE) / 2;

  public static final int TEXT_MARGIN = 4;

  public static final int NAME_WIDTH =
      DELETE_X - TerminalSettingsLayout.PANEL_X - CONTROL_MARGIN - TEXT_MARGIN;

  private TagsSectionLayout() {}

  public static int deleteY(int row) {
    return TerminalSettingsLayout.rowY(row) + CONTROL_INSET;
  }

  public static TagRowRegion regionAt(int windowX, int windowY) {
    final int row = TerminalSettingsLayout.rowAt(windowX, windowY);
    if (row == TerminalSettingsLayout.NO_ROW || row < FIRST_TAG_ROW) {
      return TagRowRegion.NONE;
    }
    final int top = deleteY(row);
    if (windowX >= DELETE_X
        && windowX < DELETE_X + CONTROL_SIZE
        && windowY >= top
        && windowY < top + CONTROL_SIZE) {
      return TagRowRegion.DELETE;
    }
    return TagRowRegion.NAME;
  }

  public static boolean isOverNewTag(int windowX, int windowY) {
    return TerminalSettingsLayout.rowAt(windowX, windowY) == NEW_TAG_ROW;
  }

  public static int tagIndexAt(int windowX, int windowY, int firstVisible) {
    final int row = TerminalSettingsLayout.rowAt(windowX, windowY);
    if (row == TerminalSettingsLayout.NO_ROW || row < FIRST_TAG_ROW) {
      return TerminalSettingsLayout.NO_ROW;
    }
    return firstVisible + row - FIRST_TAG_ROW;
  }
}
