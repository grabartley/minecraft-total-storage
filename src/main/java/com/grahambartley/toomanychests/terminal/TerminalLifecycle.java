package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.lifecycle.NextTick;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TerminalLifecycle {
  private TerminalLifecycle() {}

  public static void register() {
    ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(TerminalLifecycle::blockEntityLoaded);
    ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(TerminalLifecycle::blockEntityUnloaded);
  }

  public static void loaded(ServerWorld world, TerminalBlockEntity terminal) {
    if (world == null || terminal == null) {
      return;
    }
    NextTick.queue()
        .submit(TerminalAddress.of(world, terminal.getPos()), () -> start(world, terminal));
  }

  public static void unloaded(ServerWorld world, BlockPos pos) {
    if (world == null || pos == null) {
      return;
    }
    NextTick.queue().cancel(TerminalAddress.of(world, pos));
    TerminalNetworks.power().unloaded(world, pos);
  }

  private static void start(ServerWorld world, TerminalBlockEntity terminal) {
    if (terminal.isRemoved()) {
      return;
    }
    TerminalNetworks.power().loaded(world, terminal.getPos(), terminal.getCachedState());
  }

  private static void blockEntityLoaded(BlockEntity blockEntity, ServerWorld world) {
    if (blockEntity instanceof TerminalBlockEntity terminal) {
      loaded(world, terminal);
    }
  }

  private static void blockEntityUnloaded(BlockEntity blockEntity, ServerWorld world) {
    if (blockEntity instanceof TerminalBlockEntity) {
      unloaded(world, blockEntity.getPos());
    }
  }
}
