package com.grahambartley.toomanychests.lock;

/**
 * What the server decided about a lock toggle a client asked for.
 *
 * <p>A rejection is deliberately not an error. The only way to reach one is a forged or stale
 * packet, and disconnecting a player over a slot index would turn a harmless desync into a kick.
 */
public enum LockToggleOutcome {
  REJECTED,
  LOCKED,
  UNLOCKED;

  public boolean wasApplied() {
    return this != REJECTED;
  }
}
