package com.grahambartley.totalstorage.listing;

import java.util.Objects;

public final class ListingCache<V> {
  private ListingPage<V> page = ListingPage.pending(ListingRevision.NONE);

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

  public void clear() {
    page = ListingPage.pending(ListingRevision.NONE);
  }
}
