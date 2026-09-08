package com.grahambartley.toomanychests.screenhandler;

/**
 * Where everything sits inside the settings panel, in the window's own coordinates.
 *
 * <p>Separate from {@link TerminalScreenLayout} because the two answer different questions. That
 * one sizes the window from the item grid outwards and decides where a slot is clickable; this one
 * divides the recess that window leaves into sections and rows. They meet only where this panel is
 * anchored, which is on the grid, since the two views share one recess.
 *
 * <p>The settings view is split into sections reached from a strip along its top. The recess is a
 * hundred pixels tall and the window cannot grow, since it already stands at the height vanilla
 * will draw one at on a 320x240 screen. One column holding both the terminal's settings and its
 * container list therefore has to shrink one of them to nothing: three settings at a readable
 * twenty pixels leave room for two containers, and four containers leave the settings at twelve
 * pixels with controls too small to aim at.
 *
 * <p>Splitting them costs fourteen pixels once and gives each section eighty-four, which is four
 * rows that a player can read and hit. It is also where a third section goes when tags arrive.
 */
public final class TerminalSettingsLayout {
  /** Returned by {@link #rowAt} and {@link #sectionTabAt} for a position that is over neither. */
  public static final int NO_ROW = -1;

  /**
   * The settings panel is drawn into the same recess the grid uses, since only one view is on
   * screen at a time and a second recess would be a second thing to keep the same size.
   */
  public static final int PANEL_X = TerminalScreenLayout.GRID_X;

  public static final int PANEL_Y = TerminalScreenLayout.GRID_Y;

  public static final int PANEL_WIDTH =
      TerminalScreenLayout.GRID_WIDTH
          + TerminalScreenLayout.SCROLLBAR_GAP
          + TerminalScreenLayout.SCROLLBAR_WIDTH;

  public static final int PANEL_HEIGHT = TerminalScreenLayout.GRID_HEIGHT;

  public static final int SECTION_COUNT = 2;

  public static final int SECTION_TAB_HEIGHT = 14;

  /** Air under the strip, so the section it opens does not look joined to the tab. */
  public static final int SECTION_GAP = 2;

  public static final int SECTION_TAB_WIDTH = PANEL_WIDTH / SECTION_COUNT;

  public static final int CONTENT_Y = PANEL_Y + SECTION_TAB_HEIGHT + SECTION_GAP;

  public static final int CONTENT_HEIGHT = PANEL_HEIGHT - SECTION_TAB_HEIGHT - SECTION_GAP;

  /**
   * Tall enough for a sixteen pixel item icon with air around it, and for a control big enough to
   * aim at. This is the number that decides whether the panel reads as cramped.
   */
  public static final int ROW_HEIGHT = 20;

  public static final int ROWS = CONTENT_HEIGHT / ROW_HEIGHT;

  /**
   * A container row leaves the gutter to its scrollbar, the way the item grid does. A settings row
   * has no list to scroll and takes the whole panel, so the terminal section shows no empty channel
   * down its side.
   */
  public static final int ROW_WIDTH = TerminalScreenLayout.GRID_WIDTH;

  public static final int FULL_ROW_WIDTH = PANEL_WIDTH;

  /** A control on a settings row, inset so it does not touch the row's own edges. */
  public static final int CONTROL_SIZE = 16;

  public static final int CONTROL_INSET = (ROW_HEIGHT - CONTROL_SIZE) / 2;

  private TerminalSettingsLayout() {}

  public static int sectionTabX(int section) {
    requireSection(section);
    return PANEL_X + section * SECTION_TAB_WIDTH;
  }

  /** Which section tab a point is over, or {@link #NO_ROW} when it is over none. */
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

  /**
   * Which row of the open section a point is over, or {@link #NO_ROW} when it is over none.
   *
   * <p>Measured against the whole panel. A section with a scrollbar rejects the gutter itself,
   * since only that section has one.
   */
  public static int rowAt(int windowX, int windowY) {
    if (windowX < PANEL_X || windowX >= PANEL_X + FULL_ROW_WIDTH) {
      return NO_ROW;
    }
    final int row = Math.floorDiv(windowY - CONTENT_Y, ROW_HEIGHT);
    return windowY < CONTENT_Y || row >= ROWS ? NO_ROW : row;
  }

  /** Whether a point is over the container list's scrollbar rather than over a row. */
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
