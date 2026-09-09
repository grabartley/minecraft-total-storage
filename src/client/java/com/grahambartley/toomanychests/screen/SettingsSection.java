package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsLayout;

public enum SettingsSection {
  TERMINAL,

  CONTAINERS,

  TAGS;

  public int index() {
    return ordinal();
  }

  public String label() {
    return switch (this) {
      case TERMINAL -> "section.terminal";
      case CONTAINERS -> "section.containers";
      case TAGS -> "section.tags";
    };
  }

  public static SettingsSection at(int windowX, int windowY) {
    final int index = TerminalSettingsLayout.sectionTabAt(windowX, windowY);
    return index == TerminalSettingsLayout.NO_ROW ? null : values()[index];
  }
}
