package com.grahambartley.toomanychests.access;

public enum TrustEditOutcome {
  ADDED,

  REMOVED,

  ALREADY_TRUSTED,

  NOT_TRUSTED,

  UNKNOWN_PLAYER,

  DENIED;

  public boolean changedTheList() {
    return this == ADDED || this == REMOVED;
  }

  public boolean keepsTheTypedName() {
    return this == UNKNOWN_PLAYER || this == ALREADY_TRUSTED;
  }
}
