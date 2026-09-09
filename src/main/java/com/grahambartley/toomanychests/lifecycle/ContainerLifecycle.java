package com.grahambartley.toomanychests.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public final class ContainerLifecycle {
  private static final ContainerLifecycleRegistry REGISTRY = new ContainerLifecycleRegistry();

  private ContainerLifecycle() {}

  public static ContainerLifecycleRegistry registry() {
    return REGISTRY;
  }

  public static void register() {
    ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(ContainerLifecycle::containerRemoved);
    ServerChunkEvents.CHUNK_UNLOAD.register(ContainerLifecycle::chunkUnloaded);
    ServerChunkEvents.CHUNK_LOAD.register(ContainerLifecycle::chunkLoaded);
  }

  private static void containerRemoved(BlockEntity blockEntity, ServerWorld world) {
    if (blockEntity == null || world == null) {
      return;
    }
    REGISTRY.containerRemoved(world.getRegistryKey(), blockEntity.getPos());
  }

  private static void chunkUnloaded(ServerWorld world, WorldChunk chunk) {
    if (world == null || chunk == null) {
      return;
    }
    NextTick.queue().cancel(new ChunkArrival(world.getRegistryKey(), chunk.getPos()));
    REGISTRY.chunkUnloaded(world.getRegistryKey(), chunk.getPos());
  }

  private static void chunkLoaded(ServerWorld world, WorldChunk chunk) {
    if (world == null || chunk == null) {
      return;
    }
    final RegistryKey<World> worldKey = world.getRegistryKey();
    final ChunkPos chunkPos = chunk.getPos();
    NextTick.queue()
        .submit(
            new ChunkArrival(worldKey, chunkPos), () -> REGISTRY.chunkLoaded(worldKey, chunkPos));
  }

  private record ChunkArrival(RegistryKey<World> worldKey, ChunkPos chunkPos) {}
}
