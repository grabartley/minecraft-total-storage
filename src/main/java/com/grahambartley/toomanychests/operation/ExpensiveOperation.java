package com.grahambartley.toomanychests.operation;

public enum ExpensiveOperation {
  RESCAN,

  DEPOSIT_ALL,

  ASSIGN_MATCHING;

  public String id() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
