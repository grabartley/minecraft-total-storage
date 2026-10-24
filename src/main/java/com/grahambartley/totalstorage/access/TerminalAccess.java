package com.grahambartley.totalstorage.access;

public enum TerminalAccess {
  OWNER,

  TRUSTED,

  OPEN,

  UNOWNED,

  OPERATOR,

  DENIED;

  public boolean allowed() {
    return this != DENIED;
  }

  public boolean viaOperator() {
    return this == OPERATOR;
  }
}
