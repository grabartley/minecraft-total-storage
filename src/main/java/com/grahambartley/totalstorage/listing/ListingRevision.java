package com.grahambartley.totalstorage.listing;

public final class ListingRevision {
  public static final long NONE = 0L;

  private long issued = NONE;

  public long next() {
    return ++issued;
  }

  public long current() {
    return issued;
  }

  public static boolean supersedes(long arriving, long accepted) {
    return arriving > accepted;
  }
}
