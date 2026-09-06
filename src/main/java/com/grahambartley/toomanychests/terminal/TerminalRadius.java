package com.grahambartley.toomanychests.terminal;

/**
 * A terminal's reach once the server has had its say: what was asked for, and what is allowed.
 *
 * <p>The two are kept apart rather than collapsed into one number because the server maximum moves.
 * A terminal reaching less than it was configured to is being clamped rather than reconfigured, and
 * gets its reach back the moment the maximum goes up again.
 */
public record TerminalRadius(int requested, int blocks) {

  public static TerminalRadius of(int requested, int serverMaximum) {
    final int ceiling =
        Math.max(TerminalSettings.MIN_RADIUS, Math.min(TerminalSettings.MAX_RADIUS, serverMaximum));
    return new TerminalRadius(
        requested, Math.max(TerminalSettings.MIN_RADIUS, Math.min(ceiling, requested)));
  }

  /** The radius a scan starting right now would walk. */
  public static int effective(int configured, int serverMaximum) {
    return of(configured, serverMaximum).blocks();
  }

  public boolean clamped() {
    return blocks != requested;
  }
}
