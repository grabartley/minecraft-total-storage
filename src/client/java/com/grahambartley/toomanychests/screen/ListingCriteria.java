package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.listing.ListingWindow;

public final class ListingCriteria {
  private ListingWindow window = ListingWindow.EMPTY;
  private String search = "";

  public ListingWindow window() {
    return window;
  }

  public String search() {
    return search;
  }

  public boolean isNewQuery(String wanted) {
    return !search.equals(normalised(wanted));
  }

  public boolean needsRequest(ListingWindow wantedWindow, String wantedSearch) {
    return isNewQuery(wantedSearch) || !window.equals(orEmpty(wantedWindow));
  }

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
