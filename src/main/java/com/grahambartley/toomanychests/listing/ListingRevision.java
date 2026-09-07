package com.grahambartley.toomanychests.listing;

/**
 * Which of two listings is the newer one.
 *
 * <p>A listing is requested and answered asynchronously, and a player scrolling quickly has several
 * requests in flight at once. Nothing guarantees the answers arrive in the order they were asked
 * for, and a page that lands after a newer one would redraw the grid with results the player has
 * already scrolled past. Stamping every page settles it without the client having to reason about
 * what it asked for.
 *
 * <p>One issuer per open screen, so the numbers only ever have to be comparable against that
 * screen's own.
 */
public final class ListingRevision {
  /** Older than every issued revision, which is what a screen holding no page has accepted. */
  public static final long NONE = 0L;

  private long issued = NONE;

  /** Stamps the next page. Never returns {@link #NONE}, so any real page beats holding none. */
  public long next() {
    return ++issued;
  }

  public long current() {
    return issued;
  }

  /** Whether a page that has just arrived is newer than the one already being shown. */
  public static boolean supersedes(long arriving, long accepted) {
    return arriving > accepted;
  }
}
