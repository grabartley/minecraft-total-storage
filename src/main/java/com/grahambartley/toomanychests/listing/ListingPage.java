package com.grahambartley.toomanychests.listing;

import java.util.List;
import java.util.Objects;

public record ListingPage<V>(
    long revision, ListingStatus status, int offset, int totalMatching, List<ListingRow<V>> rows) {
  public ListingPage {
    Objects.requireNonNull(status, "status");
    Objects.requireNonNull(rows, "rows");
    if (revision < 0L) {
      throw new IllegalArgumentException("revision must not be negative: " + revision);
    }

    if (rows.size() > ListingWindow.MAX_LENGTH) {
      throw new IllegalArgumentException("a page may not carry " + rows.size() + " rows");
    }
    offset = Math.max(0, offset);
    totalMatching = Math.max(0, totalMatching);
    rows = List.copyOf(rows);
  }

  public static <V> ListingPage<V> pending(long revision) {
    return new ListingPage<>(revision, ListingStatus.PENDING, 0, 0, List.of());
  }

  public static <V> ListingPage<V> offline(long revision) {
    return new ListingPage<>(revision, ListingStatus.OFFLINE, 0, 0, List.of());
  }

  public int size() {
    return rows.size();
  }

  public boolean isEmpty() {
    return rows.isEmpty();
  }

  public ListingRow<V> at(int index) {
    final int within = index - offset;
    return within < 0 || within >= rows.size() ? null : rows.get(within);
  }
}
