package com.grahambartley.toomanychests.operation;

public enum QuickStackResult {
  STACKED,

  NOTHING_TO_STACK,

  RATE_LIMITED,

  REFUSED;

  public boolean movedAnything() {
    return this == STACKED;
  }
}
