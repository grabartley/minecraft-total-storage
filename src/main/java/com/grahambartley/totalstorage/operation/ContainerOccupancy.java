package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.index.NetworkIndex;
import com.grahambartley.totalstorage.index.NetworkIndexEntry;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class ContainerOccupancy<V> {
  private final NetworkIndex<V> index;
  private final Set<StorageOrigin> filled = new HashSet<>();
  private final Map<V, Set<StorageOrigin>> holders = new HashMap<>();

  public ContainerOccupancy(NetworkIndex<V> index) {
    this.index = Objects.requireNonNull(index, "index");
  }

  public ContainerContents contentsOf(StorageOrigin container, V variant) {
    if (holdersOf(variant).contains(container)) {
      return ContainerContents.HOLDS_ITEM;
    }
    return index.occupied().contains(container) || filled.contains(container)
        ? ContainerContents.OTHER_ITEMS
        : ContainerContents.EMPTY;
  }

  public boolean stored(StorageOrigin container, V variant) {
    Objects.requireNonNull(container, "container");
    final boolean newHolder = holdersOf(variant).add(container);
    final boolean newlyFilled = !index.occupied().contains(container) && filled.add(container);
    return newHolder || newlyFilled;
  }

  private Set<StorageOrigin> holdersOf(V variant) {
    return holders.computeIfAbsent(
        variant,
        key -> new HashSet<>(index.entry(key).map(NetworkIndexEntry::origins).orElse(List.of())));
  }
}
