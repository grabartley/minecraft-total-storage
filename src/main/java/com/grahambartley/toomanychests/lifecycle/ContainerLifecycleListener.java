package com.grahambartley.toomanychests.lifecycle;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public interface ContainerLifecycleListener {
  void containerRemoved(StorageOrigin origin);

  void chunkUnloaded(RegistryKey<World> worldKey, ChunkPos chunkPos);

  void chunkLoaded(RegistryKey<World> worldKey, ChunkPos chunkPos);

  void containerPlaced(RegistryKey<World> worldKey, ChunkPos chunkPos);
}
