package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * How a terminal's network survives the chunk it lives in coming and going.
 *
 * <p>A neighbour update is the only thing that can change a terminal's power, and a terminal in an
 * unloaded chunk gets none. Its block state remembers that it was powered, so the block entity
 * arriving is the moment to start the network again, and the block entity leaving is the moment to
 * stop it, whether the chunk unloaded or a player broke the block.
 */
public final class TerminalLifecycle {
  private TerminalLifecycle() {}

  public static void register() {
    ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(TerminalLifecycle::terminalLoaded);
    ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(TerminalLifecycle::terminalUnloaded);
  }

  private static void terminalLoaded(BlockEntity blockEntity, ServerWorld world) {
    if (!(blockEntity instanceof TerminalBlockEntity) || world == null) {
      return;
    }
    TerminalNetworks.power()
        .loaded(world, blockEntity.getPos(), world.getBlockState(blockEntity.getPos()));
  }

  private static void terminalUnloaded(BlockEntity blockEntity, ServerWorld world) {
    if (!(blockEntity instanceof TerminalBlockEntity) || world == null) {
      return;
    }
    TerminalNetworks.power().unloaded(world, blockEntity.getPos());
  }
}
