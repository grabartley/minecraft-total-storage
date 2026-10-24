package com.grahambartley.totalstorage.index;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;

public final class NetworkIndexOrdering {
  private NetworkIndexOrdering() {}

  public static <V> Comparator<NetworkIndexEntry<V>> byKey(Function<V, String> sortKey) {
    Objects.requireNonNull(sortKey, "sortKey");
    return Comparator.<NetworkIndexEntry<V>, String>comparing(
            entry -> Objects.requireNonNull(sortKey.apply(entry.variant()), "sortKey"))
        .thenComparing(Comparator.comparingLong(NetworkIndexEntry<V>::totalCount).reversed())
        .thenComparing(Comparator.comparingInt(NetworkIndexEntry<V>::providerCount).reversed());
  }
}
