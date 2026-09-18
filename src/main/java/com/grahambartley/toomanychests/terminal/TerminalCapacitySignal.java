package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.TerminalBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TerminalCapacitySignal {
  private TerminalCapacitySignal() {}

  public static boolean warns(BlockState state, NetworkCapacity capacity, int warningPercent) {
    return state != null
        && state.contains(TerminalBlock.POWERED)
        && state.get(TerminalBlock.POWERED)
        && capacity != null
        && capacity.isNearCapacity(warningPercent);
  }

  public static boolean needsWriting(BlockState state, boolean warning) {
    return state != null
        && state.contains(TerminalBlock.NEAR_CAPACITY)
        && state.get(TerminalBlock.NEAR_CAPACITY) != warning;
  }

  public static boolean show(ServerWorld world, BlockPos terminal, boolean warning) {
    if (world == null || terminal == null) {
      return false;
    }
    final BlockState state = world.getBlockState(terminal);
    if (!needsWriting(state, warning)) {
      return false;
    }
    world.setBlockState(
        terminal, state.with(TerminalBlock.NEAR_CAPACITY, warning), Block.NOTIFY_ALL);
    return true;
  }
}
