package com.grahambartley.totalstorage.listing;

public enum ListingRequestOutcome {
  IGNORED,

  ANSWERED,

  ANSWERED_ON_A_NEW_SEARCH;

  public boolean wasAnswered() {
    return this != IGNORED;
  }

  public boolean movedTheSearch() {
    return this == ANSWERED_ON_A_NEW_SEARCH;
  }
}
