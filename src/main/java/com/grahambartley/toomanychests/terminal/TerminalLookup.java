package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Finding the terminal a position is supposed to hold, if it still holds one.
 *
 * <p>Every service reaching a terminal is answering a request that named a position rather than a
 * block entity, so all of them have to survive being pointed at a hole.
 */
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
