package com.grahambartley.toomanychests.listing;

import java.util.Objects;

/**
 * The page a screen is currently drawing, and the rule for when a newly arrived one replaces it.
 *
 * <p>A display cache and nothing more. It is never consulted to decide what a withdrawal may take:
 * per ADR 0009 the client sends what it wants and the server decides against the live containers,
 * so a cache that has gone stale costs a refused request rather than a duplicated item.
 *
 * <p>Pages that are not newer than the one already held are dropped rather than drawn. Several
 * requests are in flight whenever a player scrolls, and without this the grid would flick back to
 * an earlier page each time a slower answer overtook a faster one.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 */
public final class ListingCache<V> {
  private ListingPage<V> page = ListingPage.empty(ListingRevision.NONE);

  /** Returns whether the page was taken, so a caller drawing it knows there is news. */
  public boolean accept(ListingPage<V> arriving) {
    Objects.requireNonNull(arriving, "arriving");
    if (!ListingRevision.supersedes(arriving.revision(), page.revision())) {
      return false;
    }
    page = arriving;
    return true;
  }

  public ListingPage<V> page() {
    return page;
  }

  /**
   * Forgets the page and the revision with it, which is what closing a screen or leaving a server
   * amounts to. The next screen's revisions start from the beginning, so keeping the old high water
   * mark would make every one of its pages look stale.
   */
  public void clear() {
    page = ListingPage.empty(ListingRevision.NONE);
  }
}
