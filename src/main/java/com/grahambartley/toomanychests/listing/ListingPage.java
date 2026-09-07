package com.grahambartley.toomanychests.listing;

import java.util.List;
import java.util.Objects;

/**
 * One answer to one listing request: the rows asked for, where they sit in the whole result, and
 * why there are that many of them.
 *
 * <p>The total is carried alongside the rows because a scrollbar cannot be drawn from a page that
 * only knows its own length. It counts what the search and filter matched rather than what the
 * network holds, so it is the number of rows the player could scroll through right now.
 *
 * <p>The status is carried for the same reason in reverse: a page of no rows is not
 * self-explaining, and per {@link ListingStatus} only the server knows which of the empty cases it
 * is.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 * @param revision which listing this is, per {@link ListingRevision}
 * @param status why the page holds the rows it holds
 * @param offset how many matching rows come before the first one here
 * @param totalMatching how many rows the criteria matched in total
 * @param rows the rows themselves, already ordered
 */
public record ListingPage<V>(
    long revision, ListingStatus status, int offset, int totalMatching, List<ListingRow<V>> rows) {
  public ListingPage {
    Objects.requireNonNull(status, "status");
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

  /** What a screen holds before anything has answered it. */
  public static <V> ListingPage<V> pending(long revision) {
    return new ListingPage<>(revision, ListingStatus.PENDING, 0, 0, List.of());
  }

  /** What a terminal running no network answers with, so a screen still hears back. */
  public static <V> ListingPage<V> offline(long revision) {
    return new ListingPage<>(revision, ListingStatus.OFFLINE, 0, 0, List.of());
  }

  public int size() {
    return rows.size();
  }

  public boolean isEmpty() {
    return rows.isEmpty();
  }

  /**
   * The row at a position in the whole result, or {@code null} when this page does not cover it.
   *
   * <p>A grid draws from wherever the player has scrolled to, and the page in hand answers an
   * earlier scroll position for as long as a newer request is in flight. Addressing rows absolutely
   * rather than by their position within the page is what lets the cells that do overlap stay
   * correct instead of the whole grid shifting.
   */
  public ListingRow<V> at(int index) {
    final int within = index - offset;
    return within < 0 || within >= rows.size() ? null : rows.get(within);
  }
}
