package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.block.entity.TerminalBlockEntity;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class TerminalLookup {
  private TerminalLookup() {}

  public static Optional<TerminalBlockEntity> at(World world, BlockPos pos) {
    if (world == null || pos == null) {
      return Optional.empty();
    }
    return world.getBlockEntity(pos) instanceof TerminalBlockEntity terminal
        ? Optional.of(terminal)
        : Optional.empty();
  }

  public static Optional<TerminalSettings> settingsAt(World world, BlockPos pos) {
    return at(world, pos).map(TerminalBlockEntity::settings);
  }

  public static Optional<TerminalAddress> addressOf(ServerWorld world, BlockPos pos) {
    return at(world, pos).map(terminal -> TerminalAddress.of(world, pos));
  }
}
