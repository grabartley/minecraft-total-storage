package com.grahambartley.totalstorage.mutation;

import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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

  public void watchEverywhere(ContainerMutationListener listener) {
    Objects.requireNonNull(listener, "listener");
    everywhere.add(listener);
  }

  public void stopWatchingEverywhere(ContainerMutationListener listener) {
    everywhere.remove(listener);
  }

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

    if (!everywhere.isEmpty()) {
      for (final ContainerMutationListener listener : List.copyOf(everywhere)) {
        listener.containerChanged(origin);
      }
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
