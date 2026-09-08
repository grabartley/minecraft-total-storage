package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;

/**
 * The controls that sit on a settings row, and which one a click landed on.
 *
 * <p>A row is twenty pixels tall so a control can be sixteen: big enough to aim at without care,
 * where the ten pixel squares this replaced were not. The two radius controls sit at opposite ends
 * of their row rather than side by side, so hitting the wrong one takes a deliberate mistake.
 *
 * <p>Positions are worked out from the row's geometry rather than written down, so a row that moves
 * takes its controls with it. Deciding it here rather than inside a mouse handler is what lets the
 * hit test be proven against every corner without a running client.
 */
public enum SettingsControl {
  RADIUS_DOWN,
  RADIUS_UP,
  NONE;

  public static int size() {
    return TerminalSettingsLayout.CONTROL_SIZE;
  }

  public static int radiusDownX() {
    return TerminalSettingsLayout.PANEL_X + TerminalSettingsLayout.CONTROL_INSET;
  }

  public static int radiusUpX() {
    return TerminalSettingsLayout.PANEL_X
        + TerminalSettingsLayout.FULL_ROW_WIDTH
        - TerminalSettingsLayout.CONTROL_INSET
        - size();
  }

  public static int radiusY() {
    return TerminalSettingsLayout.rowY(TerminalSetting.RADIUS.row())
        + TerminalSettingsLayout.CONTROL_INSET;
  }

  /** Which control a point in the window is over, on the terminal section only. */
  public static SettingsControl at(int windowX, int windowY) {
    if (over(windowX, windowY, radiusDownX(), radiusY())) {
      return RADIUS_DOWN;
    }
    return over(windowX, windowY, radiusUpX(), radiusY()) ? RADIUS_UP : NONE;
  }

  public boolean isSomething() {
    return this != NONE;
  }

  private static boolean over(int windowX, int windowY, int left, int top) {
    return windowX >= left && windowX < left + size() && windowY >= top && windowY < top + size();
  }
}
