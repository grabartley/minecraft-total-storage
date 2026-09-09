package com.grahambartley.toomanychests.index;

import com.grahambartley.toomanychests.mutation.ContainerMutationListener;
import com.grahambartley.toomanychests.mutation.ContainerMutationRegistry;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerScanResult;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public final class NetworkIndexSession<V> implements ContainerMutationListener {
  private final ContainerMutationRegistry registry;
  private final NetworkIndexCache<V> cache;
  private final List<StorageOrigin> watched = new ArrayList<>();

  private boolean active;

  public NetworkIndexSession(
      ContainerMutationRegistry registry,
      Function<StorageOrigin, Map<V, Long>> contentsReader,
      Comparator<NetworkIndexEntry<V>> ordering) {
    this.registry = Objects.requireNonNull(registry, "registry");
    this.cache = new NetworkIndexCache<>(contentsReader, ordering);
  }

  public static NetworkIndexSession<ItemVariant> forWorld(World world) {
    return new NetworkIndexSession<>(
        ContainerMutations.registry(),
        new ContainerContentsReader(world),
        NetworkIndexOrdering.byKey(new ItemVariantSortKey()));
  }

  public void begin(ContainerScanResult scan) {
    Objects.requireNonNull(scan, "scan");
    begin(scan.providers().stream().map(StorageProvider::origin).toList());
  }

  public void begin(Collection<StorageOrigin> origins) {
    Objects.requireNonNull(origins, "origins");
    end();
    watched.addAll(origins);
    cache.track(watched);
    for (final StorageOrigin origin : watched) {
      registry.watch(origin, this);
    }
    active = true;
  }

  public void end() {
    for (final StorageOrigin origin : watched) {
      registry.unwatch(origin, this);
    }
    watched.clear();
    cache.track(List.of());
    active = false;
  }

  public void admit(StorageOrigin origin) {
    Objects.requireNonNull(origin, "origin");
    if (!active || watched.contains(origin)) {
      return;
    }
    watched.add(origin);
    cache.adopt(origin);
    registry.watch(origin, this);
  }

  public void drop(StorageOrigin origin) {
    if (origin == null || !watched.remove(origin)) {
      return;
    }
    registry.unwatch(origin, this);
    cache.forget(origin);
  }

  public List<StorageOrigin> dropChunk(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    if (!active || worldKey == null || chunkPos == null) {
      return List.of();
    }
    final List<StorageOrigin> dropping =
        watched.stream()
            .filter(
                origin ->
                    origin instanceof BlockStorageOrigin block && block.isIn(worldKey, chunkPos))
            .toList();
    dropping.forEach(this::drop);
    return dropping;
  }

  public void reconcile(ContainerScanResult scan) {
    Objects.requireNonNull(scan, "scan");
    reconcile(scan.providers().stream().map(StorageProvider::origin).toList());
  }

  public void reconcile(Collection<StorageOrigin> current) {
    Objects.requireNonNull(current, "current");
    if (!active) {
      return;
    }
    final Set<StorageOrigin> incoming = new LinkedHashSet<>(current);
    for (final StorageOrigin origin : List.copyOf(watched)) {
      if (!incoming.contains(origin)) {
        drop(origin);
      }
    }
    incoming.forEach(this::admit);
  }

  public boolean isWatching(StorageOrigin origin) {
    return watched.contains(origin);
  }

  public NetworkIndex<V> index() {
    return active ? cache.index() : NetworkIndex.empty();
  }

  @Override
  public void containerChanged(StorageOrigin origin) {
    cache.invalidate(origin);
  }

  public boolean isStale() {
    return active && cache.isStale();
  }

  public boolean isActive() {
    return active;
  }

  public List<StorageOrigin> watchedContainers() {
    return List.copyOf(watched);
  }

  public int recomputeCount() {
    return cache.recomputeCount();
  }

  public int containerReadCount() {
    return cache.containerReadCount();
  }
}
