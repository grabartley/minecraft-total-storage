package com.grahambartley.toomanychests.sort;

public enum SortOutcome {
  NO_OPEN_SCREEN,
  SCREEN_NOT_SORTABLE,
  NOTHING_TO_SORT,
  ALREADY_SORTED,
  FAILED,
  SORTED;

  public boolean wasSorted() {
    return this == SORTED;
  }

  public boolean wasRefused() {
    return this == NO_OPEN_SCREEN || this == SCREEN_NOT_SORTABLE;
  }
}
