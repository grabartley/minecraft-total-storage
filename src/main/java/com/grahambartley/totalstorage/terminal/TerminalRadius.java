package com.grahambartley.totalstorage.terminal;

public record TerminalRadius(int requested, int blocks) {
  public static TerminalRadius of(int requested, int serverMaximum) {
    final int ceiling =
        Math.max(TerminalSettings.MIN_RADIUS, Math.min(TerminalSettings.MAX_RADIUS, serverMaximum));
    return new TerminalRadius(
        requested, Math.max(TerminalSettings.MIN_RADIUS, Math.min(ceiling, requested)));
  }

  public static int effective(int configured, int serverMaximum) {
    return of(configured, serverMaximum).blocks();
  }

  public boolean clamped() {
    return blocks != requested;
  }
}
