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
  /** There was something to store and the network had nowhere to put it. */
  NO_SPACE,
  /** The request never reached a network: no screen, no permission, or no power. */
  REFUSED;

  public boolean movedAnything() {
    return this == DEPOSITED || this == PARTIAL;
  }
}
