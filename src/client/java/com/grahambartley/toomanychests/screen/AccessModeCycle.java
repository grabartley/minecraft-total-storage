package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.TerminalAccessMode;

public final class AccessModeCycle {
  private static final TerminalAccessMode[] ORDER = {
    TerminalAccessMode.OWNER, TerminalAccessMode.TRUSTED, TerminalAccessMode.OPEN
  };

  private AccessModeCycle() {}

  public static TerminalAccessMode next(TerminalAccessMode mode) {
    return ORDER[Math.floorMod(indexOf(mode) + 1, ORDER.length)];
  }

  public static TerminalAccessMode previous(TerminalAccessMode mode) {
    return ORDER[Math.floorMod(indexOf(mode) - 1, ORDER.length)];
  }

  private static int indexOf(TerminalAccessMode mode) {
    for (int index = 0; index < ORDER.length; index++) {
      if (ORDER[index] == mode) {
        return index;
      }
    }
    return 0;
  }
}
