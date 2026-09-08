package com.grahambartley.toomanychests.mutation;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Who wants to hear about which container.
 *
 * <p>Every container mutation in the world is published here, so the idle path has to be nothing:
 * with no watchers registered a publish is one {@code isEmpty} check and a return, which is what
 * lets an unpowered terminal cost literally zero.
 *
 * <p>Server side and single threaded, like the block entities that feed it.
 */
public final class ContainerMutationRegistry {
  private final Map<StorageOrigin, Set<ContainerMutationListener>> watchers = new HashMap<>();
  private final Set<ContainerMutationListener> everywhere = new LinkedHashSet<>();

  public void watch(StorageOrigin origin, ContainerMutationListener listener) {
    Objects.requireNonNull(origin, "origin");
    Objects.requireNonNull(listener, "listener");
    watchers.computeIfAbsent(origin, key -> new LinkedHashSet<>()).add(listener);
  }

  public void unwatch(StorageOrigin origin, ContainerMutationListener listener) {
    final Set<ContainerMutationListener> registered = watchers.get(origin);
    if (registered == null) {
      return;
    }
    registered.remove(listener);
    if (registered.isEmpty()) {
      watchers.remove(origin);
    }
  }

  /**
   * Listens to every container in the world rather than to named ones, for a caller that knows
   * something is about to change and cannot name where. Sorting a double chest is the case: the
   * screen names one inventory spanning two block entities, and only the mutations themselves say
   * which those were.
   *
   * <p>Meant for the length of one operation and no longer. A listener left here hears about every
   * container on the server for ever, which is the cost the per-origin map exists to avoid.
   */
  public void watchEverywhere(ContainerMutationListener listener) {
    Objects.requireNonNull(listener, "listener");
    everywhere.add(listener);
  }

  public void stopWatchingEverywhere(ContainerMutationListener listener) {
    everywhere.remove(listener);
  }

  /** Drops a listener from every container it watches, which is what powering off amounts to. */
  public void unwatchAll(ContainerMutationListener listener) {
    watchers
        .values()
        .removeIf(
            registered -> {
              registered.remove(listener);
              return registered.isEmpty();
            });
  }

  public void publish(RegistryKey<World> worldKey, BlockPos pos) {
    if (isIdle() || worldKey == null || pos == null) {
      return;
    }
    final StorageOrigin origin = new BlockStorageOrigin(worldKey, pos);
    // Copied because a listener is entitled to stop watching the container it is being told about.
    for (final ContainerMutationListener listener : List.copyOf(everywhere)) {
      listener.containerChanged(origin);
    }
    final Set<ContainerMutationListener> registered = watchers.get(origin);
    if (registered == null) {
      return;
    }
    for (final ContainerMutationListener listener : List.copyOf(registered)) {
      listener.containerChanged(origin);
    }
  }

  public boolean isIdle() {
    return watchers.isEmpty() && everywhere.isEmpty();
  }

  public int watchedContainerCount() {
    return watchers.size();
  }

  /**
   * How many listeners are hearing about every container rather than about named ones. Kept
   * separate from the per-origin count because a leak here is the expensive one.
   */
  public int watchedEverywhereCount() {
    return everywhere.size();
  }

  public boolean isWatched(StorageOrigin origin) {
    return watchers.containsKey(origin);
  }

  public void clear() {
    watchers.clear();
    everywhere.clear();
  }
}
