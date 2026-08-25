package com.grahambartley.toomanychests.index;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * Keeps an index current without ever looking for changes.
 *
 * <p>A mutation marks exactly one container stale. Nothing is recomputed at that moment: the work
 * happens on the next read, so a network nobody is looking at does none, and a container hammered a
 * hundred times between two reads costs one re-read rather than a hundred. That collapsing is the
 * whole debounce, and it holds for any burst within a tick or across many.
 *
 * <p>Recomputation re-reads only the stale containers and re-merges the snapshots it already has,
 * so a hopper trickling into one chest never re-reads the rest of the network and never triggers a
 * discovery scan.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 */
public final class NetworkIndexCache<V> {
  private final Function<StorageOrigin, Map<V, Long>> contentsReader;
  private final Comparator<NetworkIndexEntry<V>> ordering;

  private final Map<StorageOrigin, Map<V, Long>> contents = new LinkedHashMap<>();
  private final Set<StorageOrigin> stale = new LinkedHashSet<>();

  private NetworkIndex<V> index = NetworkIndex.empty();
  private boolean remerge;
  private int recomputeCount;
  private int containerReadCount;

  public NetworkIndexCache(
      Function<StorageOrigin, Map<V, Long>> contentsReader,
      Comparator<NetworkIndexEntry<V>> ordering) {
    this.contentsReader = Objects.requireNonNull(contentsReader, "contentsReader");
    this.ordering = Objects.requireNonNull(ordering, "ordering");
  }

  /**
   * Replaces the tracked containers wholesale, which is what a discovery scan produces. Discovery
   * order is kept, so entries the ordering cannot separate stay put between openings.
   */
  public void track(Collection<StorageOrigin> origins) {
    Objects.requireNonNull(origins, "origins");
    contents.clear();
    stale.clear();
    remerge = false;
    index = NetworkIndex.empty();
    for (final StorageOrigin origin : origins) {
      Objects.requireNonNull(origin, "origin");
      contents.put(origin, Map.of());
      stale.add(origin);
    }
  }

  /** Marks one container stale. Origins outside the network are ignored rather than adopted. */
  public void invalidate(StorageOrigin origin) {
    if (contents.containsKey(origin)) {
      stale.add(origin);
    }
  }

  /**
   * Adds one container to the tracked set, unread, which is what a chunk load produces. Already
   * tracked containers keep the snapshot they have rather than being re-read for nothing.
   */
  public void adopt(StorageOrigin origin) {
    Objects.requireNonNull(origin, "origin");
    if (contents.containsKey(origin)) {
      return;
    }
    contents.put(origin, Map.of());
    stale.add(origin);
  }

  /**
   * Removes one container and everything it contributed. Unlike invalidation this cannot wait for a
   * re-read to correct it: the container is gone, so the next read must not include it, and the
   * merge is forced even though nothing is stale.
   */
  public void forget(StorageOrigin origin) {
    stale.remove(origin);
    if (contents.remove(origin) != null) {
      remerge = true;
    }
  }

  public boolean isTracking(StorageOrigin origin) {
    return contents.containsKey(origin);
  }

  public NetworkIndex<V> index() {
    if (stale.isEmpty() && !remerge) {
      return index;
    }
    // Drained before reading, so a storage that marks itself dirty as it is read is picked up by
    // the next read rather than corrupting this one.
    final List<StorageOrigin> reading = List.copyOf(stale);
    stale.clear();
    remerge = false;
    for (final StorageOrigin origin : reading) {
      contents.put(origin, readContents(origin));
      containerReadCount++;
    }
    index = merge();
    recomputeCount++;
    return index;
  }

  public boolean isStale() {
    return !stale.isEmpty() || remerge;
  }

  public int trackedContainerCount() {
    return contents.size();
  }

  /** How many times the index has been rebuilt from snapshots, which idle work would inflate. */
  public int recomputeCount() {
    return recomputeCount;
  }

  /** How many container reads have happened, which a full rebuild would inflate. */
  public int containerReadCount() {
    return containerReadCount;
  }

  private Map<V, Long> readContents(StorageOrigin origin) {
    final Map<V, Long> held = contentsReader.apply(origin);
    return held == null || held.isEmpty()
        ? Map.of()
        : Collections.unmodifiableMap(new LinkedHashMap<>(held));
  }

  private NetworkIndex<V> merge() {
    final NetworkIndexBuilder<V> builder = new NetworkIndexBuilder<>();
    for (final Map.Entry<StorageOrigin, Map<V, Long>> container : contents.entrySet()) {
      for (final Map.Entry<V, Long> held : container.getValue().entrySet()) {
        builder.add(held.getKey(), held.getValue(), container.getKey());
      }
    }
    return builder.build(ordering);
  }
}
