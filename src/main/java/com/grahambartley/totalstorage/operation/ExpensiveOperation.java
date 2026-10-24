package com.grahambartley.totalstorage.operation;

import java.util.Locale;

public enum ExpensiveOperation {
  RESCAN,

  DEPOSIT_ALL,

  ASSIGN_MATCHING,

  SORT_CONTAINER,

  SORT_NETWORK,

  CONSOLIDATE,

  QUICK_STACK;

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }
}
