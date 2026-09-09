package com.grahambartley.toomanychests.listing;

public enum ListingStatus {
  PENDING,

  LIVE,

  EMPTY_NETWORK,

  NO_RESULTS,

  OFFLINE;

  public static ListingStatus of(boolean networkEmpty, boolean anyMatches) {
    if (anyMatches) {
      return LIVE;
    }
    return networkEmpty ? EMPTY_NETWORK : NO_RESULTS;
  }

  public boolean hasRows() {
    return this == LIVE;
  }
}
