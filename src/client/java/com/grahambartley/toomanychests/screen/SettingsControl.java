package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;

/**
 * The controls on the settings panel's header, and which one a click landed on.
 *
 * <p>The header is drawn as text with small buttons on the end of two of its lines, so there is no
 * widget to ask. Deciding it here rather than inside the panel's mouse handler is what lets the hit
 * test be proven against every corner without a running client.
 */
public enum SettingsControl {
  RADIUS_DOWN,
  RADIUS_UP,
  ACCESS_CYCLE,
  NONE;

  /** Where the radius controls sit on their row, measured from the right of the panel. */
  private static final int RADIUS_UP_RIGHT_INSET = TerminalScreenLayout.SETTINGS_CONTROL_SIZE;

  private static final int RADIUS_DOWN_RIGHT_INSET =
      2 * TerminalScreenLayout.SETTINGS_CONTROL_SIZE + 2;

  private static final int RADIUS_ROW = 0;
  private static final int ACCESS_ROW = 1;

  public static int radiusDownX() {
    return right() - RADIUS_DOWN_RIGHT_INSET;
  }

  public static int radiusUpX() {
    return right() - RADIUS_UP_RIGHT_INSET;
  }

  public static int accessCycleX() {
    return right() - RADIUS_UP_RIGHT_INSET;
  }

  public static int rowY(int row) {
    return TerminalScreenLayout.settingsRowY(row);
  }

  public static int radiusRow() {
    return RADIUS_ROW;
  }

  public static int accessRow() {
    return ACCESS_ROW;
  }

  /** Which control a point in the window is over. */
  public static SettingsControl at(int windowX, int windowY) {
    if (over(windowX, windowY, radiusDownX(), rowY(RADIUS_ROW))) {
      return RADIUS_DOWN;
    }
    if (over(windowX, windowY, radiusUpX(), rowY(RADIUS_ROW))) {
      return RADIUS_UP;
    }
    if (over(windowX, windowY, accessCycleX(), rowY(ACCESS_ROW))) {
      return ACCESS_CYCLE;
    }
    return NONE;
  }

  public boolean isSomething() {
    return this != NONE;
  }

  private static boolean over(int windowX, int windowY, int left, int top) {
    final int size = TerminalScreenLayout.SETTINGS_CONTROL_SIZE;
    return windowX >= left && windowX < left + size && windowY >= top && windowY < top + size;
  }

  private static int right() {
    return TerminalScreenLayout.SETTINGS_X + TerminalScreenLayout.SETTINGS_WIDTH;
  }
}
