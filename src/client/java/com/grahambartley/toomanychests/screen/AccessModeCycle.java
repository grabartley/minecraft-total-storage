package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.TerminalAccessMode;

/**
 * The order clicking the access control walks a terminal through.
 *
 * <p>The same shape as {@link ContainerRoleCycle}, and extracted for the same reason: an enum cycle
 * written inline in a mouse handler is a rule nobody can check against the design.
 *
 * <p>The order runs from the most closed to the most open, so a player clicking repeatedly is
 * always letting more people in rather than jumping about, and the mode they arrive at first is the
 * cautious one.
 */
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
