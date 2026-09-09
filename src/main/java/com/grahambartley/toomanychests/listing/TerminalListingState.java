package com.grahambartley.toomanychests.listing;

import com.grahambartley.toomanychests.filter.ItemFilter;

public final class TerminalListingState {
  private final ListingRevision revision = new ListingRevision();

  private ListingWindow window = ListingWindow.EMPTY;
  private ItemFilter filter = ItemFilter.NONE;

  public void requested(ListingWindow requestedWindow, ItemFilter requestedFilter) {
    window = requestedWindow == null ? ListingWindow.EMPTY : requestedWindow;
    filter = requestedFilter == null ? ItemFilter.NONE : requestedFilter;
  }

  public ListingWindow window() {
    return window;
  }

  public ItemFilter filter() {
    return filter;
  }

  public long nextRevision() {
    return revision.next();
  }

  public long revision() {
    return revision.current();
  }

  public boolean hasWindow() {
    return !window.isEmpty();
  }

  @Override
  public String toString() {
    return "TerminalListingState[window="
        + window
        + ", filter="
        + filter
        + ", revision="
        + revision.current()
        + "]";
  }
}
