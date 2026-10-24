package com.grahambartley.totalstorage.sort;

public enum StorageSortOutcome {
  SORTED,

  ALREADY_SORTED,

  EMPTY,

  EXCLUDED,

  NOT_SORTABLE,

  GONE,

  FAILED;

  public boolean wasSorted() {
    return this == SORTED;
  }
}
