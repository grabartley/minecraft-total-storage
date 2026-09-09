package com.grahambartley.toomanychests.terminal;

public enum RoleChangeOutcome {
  CHANGED,

  UNCHANGED,

  NOT_IN_NETWORK,

  NO_TERMINAL,

  DENIED;

  public boolean applied() {
    return this == CHANGED;
  }
}
