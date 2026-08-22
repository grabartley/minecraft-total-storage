package com.grahambartley.toomanychests.index;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * The aggregated view of everything a terminal can reach: one row per variant, already ordered.
 *
 * <p>Immutable and read-only. Every write goes through the transaction layer against the live
 * containers, never through here, so an index can be handed around and read from freely. It is a
 * snapshot, so an index outlives the truth it describes and callers revalidate before acting.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 */
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

  /** Every row, in the order the index was built with. */
  public List<NetworkIndexEntry<V>> entries() {
    return entries;
  }

  /** The rows a filter accepts, keeping index order. */
  public List<NetworkIndexEntry<V>> matching(Predicate<NetworkIndexEntry<V>> predicate) {
    Objects.requireNonNull(predicate, "predicate");
    final List<NetworkIndexEntry<V>> matches = new ArrayList<>();
    for (final NetworkIndexEntry<V> entry : entries) {
      if (predicate.test(entry)) {
        matches.add(entry);
      }
    }
    return List.copyOf(matches);
  }

  public Optional<NetworkIndexEntry<V>> entry(V variant) {
    return variant == null ? Optional.empty() : Optional.ofNullable(byVariant.get(variant));
  }

  /** How much of a variant the network holds, zero when it holds none. */
  public long countOf(V variant) {
    return entry(variant).map(NetworkIndexEntry::totalCount).orElse(0L);
  }

  public boolean contains(V variant) {
    return variant != null && byVariant.containsKey(variant);
  }

  /** Distinct variants, which is the number of cells the terminal grid draws. */
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
