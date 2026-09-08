package com.grahambartley.toomanychests.operation;

/**
 * An operation costly enough that a player repeating it as fast as they can click is worth
 * stopping.
 *
 * <p>Named rather than counted together, so a cooldown on one does not silence another. A player
 * who has just emptied their inventory has not thereby spent their right to rescan, and a shared
 * counter would tell them otherwise the first time they used both in the same second.
 *
 * <p>Sorting and consolidation join this list when they exist. They are named in the guards issue
 * for the same reason as these two: each one walks the whole network.
 */
public enum ExpensiveOperation {
  /** Walking the terminal's whole region again, block by block. */
  RESCAN,
  /** Routing every slot of an inventory at once, which reads every container in the network. */
  DEPOSIT_ALL;

  /** The suffix its refusal message is written under. */
  public String id() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
