package com.grahambartley.toomanychests.listing;

import com.grahambartley.toomanychests.filter.ItemFilter;
import java.util.Objects;

/**
 * What one open screen is asking the listing for: the rows it is showing, and what it narrowed to.
 *
 * <p>Held per screen rather than per terminal, because two players at one terminal scroll and
 * search independently. The terminal owns the items; a screen owns the view of them.
 *
 * <p>The criteria are kept rather than re-sent with every refresh, which is what lets the server
 * push an updated page when the network changes underneath a player who is not touching anything.
 */
public final class TerminalListingState {
  private final ListingRevision revision = new ListingRevision();

  private ListingWindow window = ListingWindow.EMPTY;
  private ItemFilter filter = ItemFilter.NONE;

  /** Records what a screen has just asked for, so a later push answers the same question. */
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

  /**
   * Whether this screen has asked for anything yet. A screen that has not is not pushed to, because
   * there is no window to answer and it will ask as soon as it is drawn.
   */
  public boolean hasWindow() {
    return !window.isEmpty();
  }

  @Override
  public boolean equals(Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  @Override
  public String toString() {
    return "TerminalListingState[window="
        + window
        + ", filter="
        + Objects.toString(filter)
        + ", revision="
        + revision.current()
        + "]";
  }
}
