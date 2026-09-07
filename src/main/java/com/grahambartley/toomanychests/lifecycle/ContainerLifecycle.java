package com.grahambartley.toomanychests.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

/**
 * The one registry the world publishes container comings and goings to.
 *
 * <p>Block removal and chunk unload are separate Fabric events for a reason: unloading a chunk does
 * not unload its block entities one by one, and removing a block entity says nothing about its
 * chunk. Both have to be listened for or one of the two ways a container can leave the network goes
 * unnoticed, which is the stale-provider case ADR 0002 exists to rule out.
 *
 * <p>Departures are published where they are announced and arrivals are not, per ADR 0026. A
 * departure reads nothing, while an arrival re-scans, and a scan asks the chunk manager questions
 * that must not be asked from inside a chunk load.
 *
 * <p>Server side and single threaded, like the world events that feed it.
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

  /** One chunk's arrival, so a chunk that comes and goes before the tick costs no scan at all. */
  private record ChunkArrival(RegistryKey<World> worldKey, ChunkPos chunkPos) {}
}
