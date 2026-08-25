package com.grahambartley.toomanychests.index;

import com.grahambartley.toomanychests.mutation.ContainerMutationListener;
import com.grahambartley.toomanychests.mutation.ContainerMutationRegistry;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.storage.ContainerScanResult;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.world.World;

/**
 * A powered terminal's live view of its network, from the one discovery scan that starts it to the
 * teardown that ends it.
 *
 * <p>Per ADR 0007 the two states are total. Between {@link #begin} and {@link #end} the session
 * holds an index kept current by container mutations alone; outside them it holds nothing and
 * listens to nothing, so an unpowered terminal is not merely cheap, it is absent.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 */
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

  /** Powering on: adopt what the scan found and listen to exactly that. */
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

  /** Powering off: every listener goes, and so does the index. */
  public void end() {
    for (final StorageOrigin origin : watched) {
      registry.unwatch(origin, this);
    }
    watched.clear();
    cache.track(List.of());
    active = false;
  }

  /** Empty until the session begins, and empty again the moment it ends. */
  public NetworkIndex<V> index() {
    return active ? cache.index() : NetworkIndex.empty();
  }

  @Override
  public void containerChanged(StorageOrigin origin) {
    cache.invalidate(origin);
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
