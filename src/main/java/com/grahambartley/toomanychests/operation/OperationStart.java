package com.grahambartley.toomanychests.operation;

public enum OperationStart {
  STARTED,

  CANCELLED,

  NOTHING_TO_DO,

  NEEDS_CONFIRMATION,

  RATE_LIMITED,

  NOT_ALLOWED,

  REFUSED;

  public boolean happened() {
    return this == STARTED || this == CANCELLED;
  }
}
