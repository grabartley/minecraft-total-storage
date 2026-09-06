package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.access.TerminalAccess;
import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * The one rule deciding whether a terminal is open to a player, asked both when the screen is
 * requested and on every tick it stays up.
 *
 * <p>Two entry points rather than one because only the first is an event. ADR 0008 makes logging
 * the operator bypass the mitigation for allowing it, and a bypass logged once per tick for as long
 * as a moderator leaves a screen open would bury the server log rather than record anything.
 *
 * <p>Power is read from the block state rather than from the network registry, because the block
 * state is what the player can see: a terminal that is lit should open, and one that is dark should
 * say why. The two are kept in step by {@link
 * com.grahambartley.toomanychests.terminal.TerminalPower}.
 */
public final class TerminalOpenGuard {

  private TerminalOpenGuard() {}

  /** The repeated check, which runs while a screen is open and therefore never logs. */
  public static TerminalOpenState check(ServerWorld world, BlockPos pos, PlayerEntity player) {
    return resolve(world, pos, TerminalAccessControl.access().decide(world, pos, player));
  }

  /** The check a request to open goes through, which is the one worth a line in the log. */
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
