package com.grahambartley.toomanychests.index;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class NetworkIndex<V> {
  private static final NetworkIndex<?> EMPTY = new NetworkIndex<>(List.of());

  private final List<NetworkIndexEntry<V>> entries;
  private final Map<V, NetworkIndexEntry<V>> byVariant;

  private NetworkIndex(List<NetworkIndexEntry<V>> orderedEntries) {
    this.entries = List.copyOf(orderedEntries);
    final Map<V, NetworkIndexEntry<V>> lookup = new LinkedHashMap<>(this.entries.size());
    for (final NetworkIndexEntry<V> entry : this.entries) {
      lookup.put(entry.variant(), entry);
    }
    this.byVariant = Map.copyOf(lookup);
  }

  static <V> NetworkIndex<V> ordered(List<NetworkIndexEntry<V>> orderedEntries) {
    Objects.requireNonNull(orderedEntries, "orderedEntries");
    return orderedEntries.isEmpty() ? empty() : new NetworkIndex<>(orderedEntries);
  }

  @SuppressWarnings("unchecked")
  public static <V> NetworkIndex<V> empty() {
    return (NetworkIndex<V>) EMPTY;
  }

  public List<NetworkIndexEntry<V>> entries() {
    return entries;
  }

  public Optional<NetworkIndexEntry<V>> entry(V variant) {
    return variant == null ? Optional.empty() : Optional.ofNullable(byVariant.get(variant));
  }

  public long countOf(V variant) {
    return entry(variant).map(NetworkIndexEntry::totalCount).orElse(0L);
  }

  public boolean contains(V variant) {
    return variant != null && byVariant.containsKey(variant);
  }

  public int variantCount() {
    return entries.size();
  }

  public long totalCount() {
    long total = 0L;
    for (final NetworkIndexEntry<V> entry : entries) {
      total = NetworkIndexCounts.saturatedAdd(total, entry.totalCount());
    }
    return total;
  }

  public boolean isEmpty() {
    return entries.isEmpty();
  }
}
