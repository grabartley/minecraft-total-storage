package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.ServerConfigService;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Reads and moves one terminal's radius.
 *
 * <p>A value above the server maximum is clamped as it is written rather than refused, so the
 * terminal ends up at a radius the server permits and the caller is handed the clamp to report. A
 * write that leaves the region a scan would walk unchanged costs no scan at all.
 */
public final class TerminalRadiusService {
  private final TerminalPower power;

  public TerminalRadiusService(TerminalPower power) {
    this.power = Objects.requireNonNull(power, "power");
  }

  /** Empty when there is no terminal there at all. */
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
    power.rescan(world, pos);
    return Optional.of(radius);
  }

  private static int serverMaximum() {
    return ServerConfigService.current().maxTerminalRadius();
  }
}
