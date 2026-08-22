package com.grahambartley.toomanychests.index;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

/**
 * The index's default ordering: sort key ascending, then the larger holding first, then the more
 * widely spread first.
 *
 * <p>This is the ordering that makes the terminal grid stable between openings, not the player
 * facing sort. The full ADR 0005 ordering, which groups by preset category and honours the display
 * name option, arrives with the sort comparator and layers on top of this.
 */
public final class NetworkIndexOrdering {
  private NetworkIndexOrdering() {}

  /**
   * @param sortKey the ascending key for a variant, registry identifier in production
   */
  public static <V> Comparator<NetworkIndexEntry<V>> byKey(Function<V, String> sortKey) {
    Objects.requireNonNull(sortKey, "sortKey");
    return Comparator.<NetworkIndexEntry<V>, String>comparing(
            entry -> Objects.requireNonNull(sortKey.apply(entry.variant()), "sortKey"))
        .thenComparing(Comparator.comparingLong(NetworkIndexEntry<V>::totalCount).reversed())
        .thenComparing(Comparator.comparingInt(NetworkIndexEntry<V>::providerCount).reversed());
  }
}
