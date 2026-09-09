package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;

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
