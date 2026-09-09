package com.grahambartley.toomanychests.screen;

public enum TerminalSetting {
  RADIUS,

  ACCESS,

  TRUSTED,

  CAPACITY;

  public int row() {
    return ordinal();
  }

  public String label() {
    return "settings." + name().toLowerCase(java.util.Locale.ROOT);
  }

  public boolean cyclesOnRowClick() {
    return this == ACCESS;
  }

  public static TerminalSetting atRow(int row) {
    return row < 0 || row >= values().length ? null : values()[row];
  }
}
