package com.grahambartley.toomanychests.lock;

public enum LockToggleOutcome {
  REJECTED,
  LOCKED,
  UNLOCKED;

  public boolean wasApplied() {
    return this != REJECTED;
  }
}
