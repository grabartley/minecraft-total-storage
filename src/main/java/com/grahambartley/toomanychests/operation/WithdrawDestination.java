package com.grahambartley.toomanychests.operation;

public enum WithdrawDestination {
  CURSOR,
  INVENTORY;

  public boolean holdsOneStackOnly() {
    return this == CURSOR;
  }
}
