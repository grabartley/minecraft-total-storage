package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingWindow;

/**
 * What the screen last asked the server for, and whether what it wants now has moved on from it.
 *
 * <p>A terminal asks for a page rather than being sent one, so something has to notice that the
 * player has scrolled or typed. Comparing what is wanted against what was last asked for is that
 * something, and it is what stops a screen re-asking for the page it is already showing every frame
 * it draws.
 *
 * <p>A new query is worth telling apart from a new window, because it sends the grid back to the
 * top: results a player has never seen have no scroll position to keep, and staying where they were
 * would show them the middle of a list that just changed underneath them.
 */
public final class ListingCriteria {
  private ListingWindow window = ListingWindow.EMPTY;
  private String search = "";

  public ListingWindow window() {
    return window;
  }

  public String search() {
    return search;
  }

  /** Whether the text has changed, which is what returns the grid to the first row. */
  public boolean isNewQuery(String wanted) {
    return !search.equals(normalised(wanted));
  }

  /** Whether what the screen wants differs from what it last asked for. */
  public boolean needsRequest(ListingWindow wantedWindow, String wantedSearch) {
    return isNewQuery(wantedSearch) || !window.equals(orEmpty(wantedWindow));
  }

  /** Records what has just been asked for, so the next frame compares against it. */
  public void requested(ListingWindow requestedWindow, String requestedSearch) {
    window = orEmpty(requestedWindow);
    search = normalised(requestedSearch);
  }

  private static ListingWindow orEmpty(ListingWindow wanted) {
    return wanted == null ? ListingWindow.EMPTY : wanted;
  }

  private static String normalised(String wanted) {
    return wanted == null ? "" : wanted;
  }
}
