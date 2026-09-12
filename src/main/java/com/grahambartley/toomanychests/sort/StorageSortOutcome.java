package com.grahambartley.toomanychests.sort;

public enum StorageSortOutcome {
  SORTED,

  ALREADY_SORTED,

  EMPTY,

  EXCLUDED,

  NOT_SORTABLE,

  FAILED;

  public boolean wasSorted() {
    return this == SORTED;
  }

  public boolean leftItWhereItWas() {
    return this == ALREADY_SORTED || this == EMPTY;
  }

  public boolean wasSkipped() {
    return this == EXCLUDED || this == NOT_SORTABLE;
  }
}
