package com.grahambartley.toomanychests.lifecycle;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * Who wants to hear that containers have appeared or vanished.
 *
 * <p>Not keyed by position, unlike the mutation registry. A removal is only interesting to a
 * network that holds the container, and only the network knows what it holds, so every subscriber
 * hears every event and answers for itself. Removals are rare enough that this costs nothing, and
 * with no subscriber a publish is one {@code isEmpty} check.
 *
 * <p>Server side and single threaded, like the world events that feed it.
 */
public final class ContainerLifecycleRegistry {
  private final Set<ContainerLifecycleListener> listeners = new LinkedHashSet<>();

  public void subscribe(ContainerLifecycleListener listener) {
    Objects.requireNonNull(listener, "listener");
    listeners.add(listener);
  }

  public void unsubscribe(ContainerLifecycleListener listener) {
    listeners.remove(listener);
  }

  public void containerRemoved(RegistryKey<World> worldKey, BlockPos pos) {
    if (listeners.isEmpty() || worldKey == null || pos == null) {
      return;
    }
    final StorageOrigin origin = new BlockStorageOrigin(worldKey, pos);
    for (final ContainerLifecycleListener listener : subscribers()) {
      listener.containerRemoved(origin);
    }
  }

  public void chunkUnloaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    if (listeners.isEmpty() || worldKey == null || chunkPos == null) {
      return;
    }
    for (final ContainerLifecycleListener listener : subscribers()) {
      listener.chunkUnloaded(worldKey, chunkPos);
    }
  }

  public void chunkLoaded(RegistryKey<World> worldKey, ChunkPos chunkPos) {
    if (listeners.isEmpty() || worldKey == null || chunkPos == null) {
      return;
    }
    for (final ContainerLifecycleListener listener : subscribers()) {
      listener.chunkLoaded(worldKey, chunkPos);
    }
  }

  public boolean isIdle() {
    return listeners.isEmpty();
  }

  public int listenerCount() {
    return listeners.size();
  }

  public boolean isSubscribed(ContainerLifecycleListener listener) {
    return listeners.contains(listener);
  }

  public void clear() {
    listeners.clear();
  }

  /** Copied because a listener is entitled to unsubscribe while being told about an event. */
  private List<ContainerLifecycleListener> subscribers() {
    return List.copyOf(listeners);
  }
}
