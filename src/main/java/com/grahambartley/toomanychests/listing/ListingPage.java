package com.grahambartley.toomanychests.listing;

import java.util.List;
import java.util.Objects;

/**
 * One answer to one listing request: the rows asked for, and where they sit in the whole result.
 *
 * <p>The total is carried alongside the rows because a scrollbar cannot be drawn from a page that
 * only knows its own length. It counts what the search and filter matched rather than what the
 * network holds, so it is the number of rows the player could scroll through right now.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 * @param revision which listing this is, per {@link ListingRevision}
 * @param offset how many matching rows come before the first one here
 * @param totalMatching how many rows the criteria matched in total
 * @param rows the rows themselves, already ordered
 */
public record ListingPage<V>(
    long revision, int offset, int totalMatching, List<ListingRow<V>> rows) {
  public ListingPage {
    Objects.requireNonNull(rows, "rows");
    if (revision < 0L) {
      throw new IllegalArgumentException("revision must not be negative: " + revision);
    }
    // The cap belongs to the page rather than to the window that usually produces one, so a page
    // built any other way cannot become a packet no client will accept.
    if (rows.size() > ListingWindow.MAX_LENGTH) {
      throw new IllegalArgumentException("a page may not carry " + rows.size() + " rows");
    }
    offset = Math.max(0, offset);
    totalMatching = Math.max(0, totalMatching);
    rows = List.copyOf(rows);
  }

  /** What a terminal running no network answers with, so a screen still hears back. */
  public static <V> ListingPage<V> empty(long revision) {
    return new ListingPage<>(revision, 0, 0, List.of());
  }

  public int size() {
    return rows.size();
  }

  public boolean isEmpty() {
    return rows.isEmpty();
  }
}
