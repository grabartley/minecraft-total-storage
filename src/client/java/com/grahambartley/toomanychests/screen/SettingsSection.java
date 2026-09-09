package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;

public enum SettingsSection {
  TERMINAL,

  CONTAINERS;

  public int index() {
    return ordinal();
  }

  public String label() {
    return this == TERMINAL ? "section.terminal" : "section.containers";
  }

  public static SettingsSection at(int windowX, int windowY) {
    final int index = TerminalSettingsLayout.sectionTabAt(windowX, windowY);
    return index == TerminalSettingsLayout.NO_ROW ? null : values()[index];
  }
}
