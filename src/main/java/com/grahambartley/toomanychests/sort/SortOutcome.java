package com.grahambartley.toomanychests.sort;

public enum SortOutcome {
  NOT_OFFERED,
  NO_OPEN_SCREEN,
  SCREEN_NOT_SORTABLE,
  RATE_LIMITED,
  NOTHING_TO_SORT,
  ALREADY_SORTED,
  FAILED,
  SORTED;

  public boolean wasSorted() {
    return this == SORTED;
  }

  public boolean wasRefused() {
    return this == NOT_OFFERED
        || this == NO_OPEN_SCREEN
        || this == SCREEN_NOT_SORTABLE
        || this == RATE_LIMITED;
  }
}
