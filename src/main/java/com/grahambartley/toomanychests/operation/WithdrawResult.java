package com.grahambartley.toomanychests.operation;

public enum WithdrawResult {
  WITHDRAWN,

  PARTIAL_NO_STOCK,

  PARTIAL_NO_ROOM,

  NOTHING_AVAILABLE,

  NO_ROOM,

  REFUSED;

  public boolean movedAnything() {
    return this == WITHDRAWN || isPartial();
  }

  public boolean isPartial() {
    return this == PARTIAL_NO_STOCK || this == PARTIAL_NO_ROOM;
  }
}
