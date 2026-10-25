package com.grahambartley.totalstorage.screen;

import java.util.Locale;

public enum TerminalSetting {
  ACCESS,

  TRUSTED,

  RADIUS;

  public int row() {
    return ordinal();
  }

  public String label() {
    return "settings." + name().toLowerCase(Locale.ROOT) + ".label";
  }

  public String description() {
    return "settings." + name().toLowerCase(Locale.ROOT) + ".description";
  }
}
