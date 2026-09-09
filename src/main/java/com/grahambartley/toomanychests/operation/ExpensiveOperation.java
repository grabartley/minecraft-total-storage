package com.grahambartley.toomanychests.operation;

public enum ExpensiveOperation {
  RESCAN,

  DEPOSIT_ALL;

  public String id() {
    return name().toLowerCase(java.util.Locale.ROOT);
  }
}
