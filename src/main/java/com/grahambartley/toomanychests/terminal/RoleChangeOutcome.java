package com.grahambartley.toomanychests.terminal;

/**
 * How an attempt to give a container a role ended.
 *
 * <p>The three refusals are told apart because they ask the player for different things. A
 * container the network cannot see needs moving or the radius widening; a terminal that is not
 * theirs needs its owner; a role it already held needs nothing at all.
 */
public enum RoleChangeOutcome {
  CHANGED,
  /** It already held that role, so nothing was written and nothing was resynced. */
  UNCHANGED,
  /** This network does not reach that container, so it has no opinion to record about it. */
  NOT_IN_NETWORK,
  /** There is no terminal at that position any more. */
  NO_TERMINAL,
  /** The caller may use this terminal but not configure it. */
  DENIED;

  public boolean applied() {
    return this == CHANGED;
  }
}
