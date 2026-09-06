package com.grahambartley.toomanychests.access;

/**
 * The answer to "may this player operate this terminal", and what granted it.
 *
 * <p>The reason is kept rather than collapsed to a boolean because the operator bypass has to be
 * logged and nothing else does, per ADR 0008.
 */
public enum TerminalAccess {
  /** The player placed the terminal. */
  OWNER,
  /** The owner put the player on the trust list. */
  TRUSTED,
  /** The terminal is open to everyone. */
  OPEN,
  /** Nobody owns the terminal, so it has nobody to lock out. */
  UNOWNED,
  /** The player is an operator and would otherwise have been refused. */
  OPERATOR,
  /** The player may not operate this terminal. */
  DENIED;

  public boolean allowed() {
    return this != DENIED;
  }

  public boolean viaOperator() {
    return this == OPERATOR;
  }
}
