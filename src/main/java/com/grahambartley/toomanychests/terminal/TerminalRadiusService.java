package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.ServerConfigService;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TerminalRadiusService {
  private final TerminalPower power;

  public TerminalRadiusService(TerminalPower power) {
    this.power = Objects.requireNonNull(power, "power");
  }

  public Optional<TerminalRadius> current(ServerWorld world, BlockPos pos) {
    return TerminalLookup.at(world, pos)
        .map(terminal -> TerminalRadius.of(terminal.settings().radius(), serverMaximum()));
  }

  public Optional<TerminalRadius> apply(ServerWorld world, BlockPos pos, int requested) {
    final Optional<TerminalBlockEntity> terminal = TerminalLookup.at(world, pos);
    if (terminal.isEmpty()) {
      return Optional.empty();
    }
    final TerminalRadius radius = TerminalRadius.of(requested, serverMaximum());
    terminal.get().updateSettings(settings -> settings.withRadius(radius.blocks()));
    power.rescanIfRegionChanged(world, pos);
    return Optional.of(radius);
  }

  private static int serverMaximum() {
    return ServerConfigService.current().maxTerminalRadius();
  }
}
