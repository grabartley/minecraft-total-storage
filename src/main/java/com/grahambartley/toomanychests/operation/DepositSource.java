package com.grahambartley.toomanychests.operation;

/**
 * Where a deposit takes items from.
 *
 * <p>Both are the player's own, which is the only place a deposit may reach. A slot index here
 * names a slot in the player's inventory rather than in the network, so ADR 0009 is untouched: the
 * server already knows every one of those slots and validates the index against the lockable range
 * before reading it.
 */
public enum DepositSource {
  /** The stack on the player's pointer. */
  CURSOR,
  /** One slot of the player's own inventory. */
  SLOT
}
