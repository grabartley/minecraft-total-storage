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

public final class NetworkIndexCache<V> {
  private final Function<StorageOrigin, Map<V, Long>> contentsReader;
  private final Comparator<NetworkIndexEntry<V>> ordering;

  private final Map<StorageOrigin, Map<V, Long>> contents = new LinkedHashMap<>();
  private final Set<StorageOrigin> stale = new LinkedHashSet<>();

  private NetworkIndex<V> index = NetworkIndex.empty();
  private boolean remerge;
  private int recomputeCount;
  private int containerReadCount;
  private int invalidationCount;

  public NetworkIndexCache(
      Function<StorageOrigin, Map<V, Long>> contentsReader,
      Comparator<NetworkIndexEntry<V>> ordering) {
    this.contentsReader = Objects.requireNonNull(contentsReader, "contentsReader");
    this.ordering = Objects.requireNonNull(ordering, "ordering");
  }

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

  public void invalidate(StorageOrigin origin) {
    if (contents.containsKey(origin)) {
      stale.add(origin);
      invalidationCount++;
    }
  }

  public void adopt(StorageOrigin origin) {
    Objects.requireNonNull(origin, "origin");
    if (contents.containsKey(origin)) {
      return;
    }
    contents.put(origin, Map.of());
    stale.add(origin);
    invalidationCount++;
  }

  public void forget(StorageOrigin origin) {
    stale.remove(origin);
    if (contents.remove(origin) != null) {
      remerge = true;
      invalidationCount++;
    }
  }

  public boolean isTracking(StorageOrigin origin) {
    return contents.containsKey(origin);
  }

  public NetworkIndex<V> index() {
    if (stale.isEmpty() && !remerge) {
      return index;
    }

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

  public int recomputeCount() {
    return recomputeCount;
  }

  public int containerReadCount() {
    return containerReadCount;
  }

  public int invalidationCount() {
    return invalidationCount;
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
