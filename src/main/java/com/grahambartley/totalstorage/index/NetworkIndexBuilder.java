package com.grahambartley.totalstorage.index;

import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class NetworkIndexBuilder<V> {
  private final Map<V, Accumulator> accumulators = new LinkedHashMap<>();

  public NetworkIndexBuilder<V> add(V variant, long amount, StorageOrigin origin) {
    Objects.requireNonNull(variant, "variant");
    Objects.requireNonNull(origin, "origin");
    if (amount < 0L) {
      throw new IllegalArgumentException("amount must not be negative: " + amount);
    }
    if (amount == 0L) {
      return this;
    }
    accumulators.computeIfAbsent(variant, key -> new Accumulator()).record(amount, origin);
    return this;
  }

  public NetworkIndex<V> build(Comparator<NetworkIndexEntry<V>> ordering) {
    Objects.requireNonNull(ordering, "ordering");
    final List<NetworkIndexEntry<V>> entries = new ArrayList<>(accumulators.size());
    for (final Map.Entry<V, Accumulator> accumulated : accumulators.entrySet()) {
      final Accumulator accumulator = accumulated.getValue();
      entries.add(
          new NetworkIndexEntry<>(
              accumulated.getKey(), accumulator.total, List.copyOf(accumulator.origins)));
    }

    entries.sort(ordering);
    return NetworkIndex.ordered(entries);
  }

  private static final class Accumulator {
    private final Set<StorageOrigin> origins = new LinkedHashSet<>();
    private long total;

    private void record(long amount, StorageOrigin origin) {
      total = NetworkIndexCounts.saturatedAdd(total, amount);
      origins.add(origin);
    }
  }
}
