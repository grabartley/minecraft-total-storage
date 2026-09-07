package com.grahambartley.toomanychests.listing;

/**
 * Why a page holds the rows it holds, which is the difference between the several ways a grid can
 * come up empty.
 *
 * <p>A screen showing nothing has to say which nothing it means. A network that holds no items, a
 * search that matched none of the items it does hold, and a terminal that has no network at all are
 * three different situations, and a player told only that the grid is empty cannot tell which one
 * they are looking at or what to do about it.
 *
 * <p>Decided on the server, because the server is the only side holding all three facts at once:
 * the live index, the criteria the screen asked under, and whether a network answered. A client
 * deducing it from a page would be guessing at the two it was never sent.
 */
public enum ListingStatus {
  /** Nothing has answered this screen yet, so the grid has no news rather than no items. */
  PENDING,

  /** A network answered and the page carries what it matched. */
  LIVE,

  /** A network answered and holds nothing at all. */
  EMPTY_NETWORK,

  /** A network answered and holds items, but none the search and filters accept. */
  NO_RESULTS,

  /**
   * No network answered, so what the screen is showing is whatever it was last told. A terminal
   * losing power or being broken under an open screen lands here.
   */
  OFFLINE;

  /**
   * Which of the answered states a page describes, given whether the network held anything and
   * whether the criteria matched any of it.
   *
   * <p>{@link #NO_RESULTS} is only reachable through a filter: an unnarrowed listing of a network
   * that holds something always matches that something.
   */
  public static ListingStatus of(boolean networkEmpty, boolean anyMatches) {
    if (anyMatches) {
      return LIVE;
    }
    return networkEmpty ? EMPTY_NETWORK : NO_RESULTS;
  }

  /** Whether the grid has rows to draw, as opposed to a reason it has none. */
  public boolean hasRows() {
    return this == LIVE;
  }
}
