package com.grahambartley.toomanychests.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;

/**
 * The one registry the world publishes container comings and goings to.
 *
 * <p>Block removal and chunk unload are separate Fabric events for a reason: unloading a chunk does
 * not unload its block entities one by one, and removing a block entity says nothing about its
 * chunk. Both have to be listened for or one of the two ways a container can leave the network goes
 * unnoticed, which is the stale-provider case ADR 0002 exists to rule out.
 */
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
    REGISTRY.chunkUnloaded(world.getRegistryKey(), chunk.getPos());
  }

  private static void chunkLoaded(ServerWorld world, WorldChunk chunk) {
    if (world == null || chunk == null) {
      return;
    }
    REGISTRY.chunkLoaded(world.getRegistryKey(), chunk.getPos());
  }
}
