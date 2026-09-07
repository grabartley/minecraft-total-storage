package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.lifecycle.NextTick;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * How a terminal's network survives the chunk it lives in coming and going.
 *
 * <p>A neighbour update is the only thing that can change a terminal's power, and a terminal in an
 * unloaded chunk gets none. Its block state remembers that it was powered, so the block entity
 * arriving is the moment to start the network again, and the block entity leaving is the moment to
 * stop it, whether the chunk unloaded or a player broke the block.
 *
 * <p>Arriving is answered on the next tick and leaving is answered at once, per ADR 0026. Fabric
 * announces the arrival from inside the chunk load, and starting a network scans for containers,
 * which asks the chunk manager about chunks it is still loading and hangs the world load outright.
 * Stopping a network reads nothing and is safe anywhere.
 */
public final class TerminalLifecycle {
  private TerminalLifecycle() {}

  public static void register() {
    ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register(TerminalLifecycle::blockEntityLoaded);
    ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register(TerminalLifecycle::blockEntityUnloaded);
  }

  /**
   * Owes the terminal a start on the next tick. The block entity itself is what is remembered, not
   * its position, so the work that comes due asks the terminal directly rather than asking the
   * chunk manager to find it again: a lookup is exactly the question that must not be asked from a
   * world callback, and a block entity already knows whether it is still there and what state it
   * holds.
   */
  public static void loaded(ServerWorld world, TerminalBlockEntity terminal) {
    if (world == null || terminal == null) {
      return;
    }
    NextTick.queue()
        .submit(TerminalAddress.of(world, terminal.getPos()), () -> start(world, terminal));
  }

  /** Withdraws a start that is still owed, so a terminal that has gone again is never woken. */
  public static void unloaded(ServerWorld world, BlockPos pos) {
    if (world == null || pos == null) {
      return;
    }
    NextTick.queue().cancel(TerminalAddress.of(world, pos));
    TerminalNetworks.power().unloaded(world, pos);
  }

  /**
   * A terminal that was broken or whose chunk went again in the meantime is marked removed by the
   * world, which is the whole of the check: the network it would have started is not owed to a
   * block entity that no longer belongs to anything.
   */
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
