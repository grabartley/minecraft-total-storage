package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.access.TerminalAccess;
import com.grahambartley.totalstorage.access.TerminalAccessControl;
import com.grahambartley.totalstorage.block.TerminalBlock;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TerminalOpenGuard {
  private TerminalOpenGuard() {}

  public static TerminalOpenState check(ServerWorld world, BlockPos pos, PlayerEntity player) {
    return resolve(world, pos, TerminalAccessControl.access().decide(world, pos, player));
  }

  public static TerminalOpenState checkForOpening(
      ServerWorld world, BlockPos pos, PlayerEntity player) {
    return resolve(world, pos, TerminalAccessControl.access().open(world, pos, player));
  }

  private static TerminalOpenState resolve(ServerWorld world, BlockPos pos, TerminalAccess access) {
    if (world == null || pos == null || TerminalLookup.at(world, pos).isEmpty()) {
      return TerminalOpenState.MISSING;
    }
    if (!isPowered(world.getBlockState(pos))) {
      return TerminalOpenState.UNPOWERED;
    }
    return access.allowed() ? TerminalOpenState.READY : TerminalOpenState.DENIED;
  }

  private static boolean isPowered(BlockState state) {
    return state.contains(TerminalBlock.POWERED) && state.get(TerminalBlock.POWERED);
  }
}
