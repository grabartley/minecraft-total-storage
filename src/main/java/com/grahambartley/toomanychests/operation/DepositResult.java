package com.grahambartley.toomanychests.operation;

/**
 * How a deposit ended.
 *
 * <p>Having nothing to give and having nowhere to put it are told apart deliberately. They look
 * identical from the outside, and a player who cannot tell which one happened learns to distrust
 * the button rather than to check their network.
 */
public enum DepositResult {
  /** Everything offered was stored. */
  DEPOSITED,
  /** Some of it was stored and the rest is still in the player's hands. */
  PARTIAL,
  /** The player offered nothing, so nothing happened. */
  NOTHING_TO_DEPOSIT,
  /**
   * The slot was locked, so the network never looked at it. Told apart from an empty one because a
   * lock that reports itself as "nothing there" teaches a player to distrust the lock rather than
   * to read it.
   */
  SLOT_LOCKED,
  /** There was something to store and the network had nowhere to put it. */
  NO_SPACE,
  /** The request never reached a network: no screen, no permission, or no power. */
  REFUSED,
  /**
   * The player asked again before the cooldown was up, so nothing was looked at. Told apart from a
   * refusal because one is about permission and the other about pace, and a player who reads "not
   * yours to use" when they simply clicked twice will go looking for the wrong problem.
   */
  RATE_LIMITED;

  public boolean movedAnything() {
    return this == DEPOSITED || this == PARTIAL;
  }
}
