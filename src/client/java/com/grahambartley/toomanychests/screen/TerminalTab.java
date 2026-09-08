package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;

/**
 * Which of the terminal's two views is on screen.
 *
 * <p>Both are drawn into the same recess, so the tab decides what the window is for at any moment
 * rather than adding anything to it. That is what keeps the terminal one window on a screen that
 * has no room for a second.
 */
public enum TerminalTab {
  /** What the network holds, which is what the terminal is for. */
  ITEMS,
  /** How the network is configured, which is what makes the first view safe to use. */
  SETTINGS;

  public int index() {
    return ordinal();
  }

  public boolean isSettings() {
    return this == SETTINGS;
  }

  /** The tab a click landed on, or empty when it landed on none. */
  public static TerminalTab at(int windowX, int windowY) {
    final int index = TerminalScreenLayout.tabAt(windowX, windowY);
    return index == TerminalScreenLayout.NO_CELL ? null : values()[index];
  }

  static {
    if (values().length != TerminalScreenLayout.TAB_COUNT) {
      throw new IllegalStateException("the tab strip is not sized for the tabs there are");
    }
  }
}
