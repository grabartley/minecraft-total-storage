package com.grahambartley.toomanychests.sort;

/**
 * What the server decided about a sort a client asked for.
 *
 * <p>Every rejection is silent for the same reason a rejected lock toggle is: the only ways to
 * reach one are a stale packet, a screen that closed a tick early, or a client sorting something it
 * should not, and none of those are worth a kick.
 */
public enum SortOutcome {
  NO_OPEN_SCREEN,
  SCREEN_NOT_SORTABLE,
  NOTHING_TO_SORT,
  FAILED,
  SORTED;

  public boolean wasSorted() {
    return this == SORTED;
  }

  /** True when the request never reached the items, so nothing in the world was touched. */
  public boolean wasRefused() {
    return this == NO_OPEN_SCREEN || this == SCREEN_NOT_SORTABLE;
  }
}
