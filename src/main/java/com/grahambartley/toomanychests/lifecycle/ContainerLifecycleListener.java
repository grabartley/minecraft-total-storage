package com.grahambartley.toomanychests.lifecycle;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

/**
 * Told that containers have joined or left the world, as opposed to changing what they hold.
 *
 * <p>Separate from {@code ContainerMutationListener} because the two answer different questions. A
 * mutation says a container's contents are stale and must be re-read; a lifecycle event says the
 * container itself may no longer be there to read, which no amount of re-reading can discover in
 * time.
 */
public interface ContainerLifecycleListener {
  void containerRemoved(StorageOrigin origin);

  void chunkUnloaded(RegistryKey<World> worldKey, ChunkPos chunkPos);

  void chunkLoaded(RegistryKey<World> worldKey, ChunkPos chunkPos);
}
