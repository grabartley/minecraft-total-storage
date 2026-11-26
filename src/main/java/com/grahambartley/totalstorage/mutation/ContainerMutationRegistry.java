package com.grahambartley.totalstorage.mutation;

import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ContainerMutationRegistry {
  private final Map<StorageOrigin, List<ContainerMutationListener>> watchers = new HashMap<>();
  private List<ContainerMutationListener> everywhere = List.of();

  public void watch(StorageOrigin origin, ContainerMutationListener listener) {
    Objects.requireNonNull(origin, "origin");
    Objects.requireNonNull(listener, "listener");
    watchers.compute(origin, (key, registered) -> plus(registered, listener));
  }

  public void unwatch(StorageOrigin origin, ContainerMutationListener listener) {
    watchers.computeIfPresent(origin, (key, registered) -> minus(registered, listener));
  }

  public void watchEverywhere(ContainerMutationListener listener) {
    Objects.requireNonNull(listener, "listener");
    everywhere = plus(everywhere, listener);
  }

  public void stopWatchingEverywhere(ContainerMutationListener listener) {
    final List<ContainerMutationListener> remaining = minus(everywhere, listener);
    everywhere = remaining == null ? List.of() : remaining;
  }

  public void unwatchAll(ContainerMutationListener listener) {
    watchers.replaceAll((origin, registered) -> minus(registered, listener));
    watchers.values().removeIf(Objects::isNull);
  }

  public void publish(RegistryKey<World> worldKey, BlockPos pos) {
    if (isIdle() || worldKey == null || pos == null) {
      return;
    }
    final StorageOrigin origin = new BlockStorageOrigin(worldKey, pos);
    for (final ContainerMutationListener listener : everywhere) {
      listener.containerChanged(origin);
    }
    final List<ContainerMutationListener> registered = watchers.get(origin);
    if (registered == null) {
      return;
    }
    for (final ContainerMutationListener listener : registered) {
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
    everywhere = List.of();
  }

  private static List<ContainerMutationListener> plus(
      List<ContainerMutationListener> registered, ContainerMutationListener listener) {
    if (registered == null) {
      return List.of(listener);
    }
    if (registered.contains(listener)) {
      return registered;
    }
    final List<ContainerMutationListener> next = new ArrayList<>(registered.size() + 1);
    next.addAll(registered);
    next.add(listener);
    return List.copyOf(next);
  }

  private static List<ContainerMutationListener> minus(
      List<ContainerMutationListener> registered, ContainerMutationListener listener) {
    if (!registered.contains(listener)) {
      return registered;
    }
    final List<ContainerMutationListener> next = new ArrayList<>(registered);
    next.remove(listener);
    return next.isEmpty() ? null : List.copyOf(next);
  }
}
