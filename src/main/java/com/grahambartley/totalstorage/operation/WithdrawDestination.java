package com.grahambartley.totalstorage.operation;

public enum WithdrawDestination {
  CURSOR,
  INVENTORY;

  public boolean holdsOneStackOnly() {
    return this == CURSOR;
  }
}
