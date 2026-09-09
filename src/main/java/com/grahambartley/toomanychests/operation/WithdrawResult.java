package com.grahambartley.toomanychests.operation;

public enum WithdrawResult {
  WITHDRAWN,

  PARTIAL,

  NOTHING_AVAILABLE,

  NO_ROOM,

  REFUSED;

  public boolean movedAnything() {
    return this == WITHDRAWN || this == PARTIAL;
  }
}
