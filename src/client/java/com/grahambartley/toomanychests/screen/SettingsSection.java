package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;

/**
 * Which half of the settings view is open.
 *
 * <p>The two are split because the recess holds a hundred pixels and the window cannot grow past
 * the height vanilla draws one at. Sharing that between three settings and a container list leaves
 * both too small to use, so each gets its own section and the strip along the top says which.
 */
public enum SettingsSection {
  /** The terminal itself: how far it reaches and who may open it. */
  TERMINAL,
  /** What it reaches, and what it may do with each of them. */
  CONTAINERS;

  public int index() {
    return ordinal();
  }

  /** The suffix its name is written under, since a tab has to say what it opens. */
  public String label() {
    return this == TERMINAL ? "section.terminal" : "section.containers";
  }

  /** The section a click landed on, or null when it landed on none. */
  public static SettingsSection at(int windowX, int windowY) {
    final int index = TerminalSettingsLayout.sectionTabAt(windowX, windowY);
    return index == TerminalSettingsLayout.NO_ROW ? null : values()[index];
  }
}
