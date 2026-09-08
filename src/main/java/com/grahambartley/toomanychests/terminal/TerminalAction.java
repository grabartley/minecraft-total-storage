package com.grahambartley.toomanychests.terminal;

/**
 * A thing a player asks a terminal to do that changes no setting.
 *
 * <p>Grouped into one request because they share every rule: both cost the terminal a walk of its
 * own region, both are meaningless on an unpowered terminal, and neither writes anything a save
 * would keep. Splitting them into a packet each would be two channels registered to carry one enum
 * between them.
 */
public enum TerminalAction {
  /** Walk the region again, for a network that has drifted from the world under it. */
  RESCAN,
  /** Show the player which containers the network is holding, in the world. */
  HIGHLIGHT
}
