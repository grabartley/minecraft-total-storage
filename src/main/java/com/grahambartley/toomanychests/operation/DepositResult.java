package com.grahambartley.toomanychests.operation;

public enum DepositResult {
  DEPOSITED,

  PARTIAL,

  NOTHING_TO_DEPOSIT,

  NO_SPACE,

  REFUSED,

  RATE_LIMITED;

  public boolean movedAnything() {
    return this == DEPOSITED || this == PARTIAL;
  }
}
