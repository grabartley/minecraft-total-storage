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
    if (watchers.isEmpty() || worldKey == null || pos == null) {
      return;
    }
    final StorageOrigin origin = new BlockStorageOrigin(worldKey, pos);
    final Set<ContainerMutationListener> registered = watchers.get(origin);
    if (registered == null) {
      return;
    }
    // Copied because a listener is entitled to stop watching the container it is being told about.
    for (final ContainerMutationListener listener : List.copyOf(registered)) {
      listener.containerChanged(origin);
    }
  }

  public boolean isIdle() {
    return watchers.isEmpty();
  }

  public int watchedContainerCount() {
    return watchers.size();
  }

  public boolean isWatched(StorageOrigin origin) {
    return watchers.containsKey(origin);
  }

  public void clear() {
    watchers.clear();
  }
}
