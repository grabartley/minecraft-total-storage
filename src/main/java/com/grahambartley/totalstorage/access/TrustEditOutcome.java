package com.grahambartley.totalstorage.access;

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
}
