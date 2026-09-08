package com.grahambartley.toomanychests.screen;

/**
 * The rows the terminal section is made of, in the order they are drawn.
 *
 * <p>Named rather than indexed so the hit test and the drawing cannot drift apart: a row is what it
 * is, rather than the third one down.
 */
public enum TerminalSetting {
  /** How far the terminal reaches, stepped by a control at each end of its row. */
  RADIUS,
  /** Who may open it, cycled by clicking the row. */
  ACCESS,
  /** Who it trusts, which is read here and edited from the command line. */
  TRUSTED;

  public int row() {
    return ordinal();
  }

  public String label() {
    return "settings." + name().toLowerCase(java.util.Locale.ROOT);
  }

  /** Whether clicking anywhere on the row changes it, as opposed to only its own controls. */
  public boolean cyclesOnRowClick() {
    return this == ACCESS;
  }

  public static TerminalSetting atRow(int row) {
    return row < 0 || row >= values().length ? null : values()[row];
  }
}
