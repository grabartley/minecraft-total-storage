package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.terminal.TerminalLookup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TerminalAdmin {
  private TerminalAdmin() {}

  public static boolean mayAdminister(ServerWorld world, BlockPos terminal, PlayerEntity player) {
    if (world == null || terminal == null || player == null) {
      return false;
    }
    return TerminalLookup.settingsAt(world, terminal)
        .map(TerminalCaller.of(player)::mayAdminister)
        .orElse(false);
  }
}
