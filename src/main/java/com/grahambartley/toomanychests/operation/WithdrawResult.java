package com.grahambartley.toomanychests.operation;

/**
 * How a withdrawal ended, told apart finely enough that the player is given a reason rather than a
 * silence.
 *
 * <p>Taking less than was asked for is not a failure. Per ADR 0009 the client's list is always a
 * little behind the network, so asking for more than is there is an ordinary race with a defined
 * answer: take what exists and say so.
 */
public enum WithdrawResult {
  /** Everything asked for was taken. */
  WITHDRAWN,
  /** Some of it was taken, because the network or the destination ran out first. */
  PARTIAL,
  /** The network holds none of it, which a stale listing can easily claim otherwise. */
  NOTHING_AVAILABLE,
  /** The network holds it, but there was nowhere to put it. */
  NO_ROOM,
  /** The request never reached a network: no screen, no permission, or no power. */
  REFUSED;

  public boolean movedAnything() {
    return this == WITHDRAWN || this == PARTIAL;
  }
}
