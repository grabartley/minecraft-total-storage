package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

/**
 * Moves one terminal's access mode and trust list.
 *
 * <p>Every write ends by sweeping the terminal's viewers, because an edit that only binds the next
 * click leaves whoever it excluded still holding an open screen. That sweep is the difference
 * between a trust list and a suggestion.
 *
 * <p>A write that changes nothing reports so and sweeps nothing, so re-trusting a player already
 * trusted costs neither a save nor a screen.
 */
public final class TerminalTrustService {
  private final TerminalAccessService access;

  public TerminalTrustService(TerminalAccessService access) {
    this.access = Objects.requireNonNull(access, "access");
  }

  /** Empty when the position holds no terminal, which every caller has to report differently. */
  public Optional<TerminalSettings> settingsAt(ServerWorld world, BlockPos pos) {
    return TerminalLookup.settingsAt(world, pos);
  }

  public boolean setAccessMode(ServerWorld world, BlockPos pos, TerminalAccessMode mode) {
    return change(world, pos, settings -> settings.withAccessMode(mode));
  }

  public boolean trust(ServerWorld world, BlockPos pos, UUID player) {
    return change(world, pos, settings -> settings.withTrusted(player));
  }

  public boolean untrust(ServerWorld world, BlockPos pos, UUID player) {
    return change(world, pos, settings -> settings.withoutTrusted(player));
  }

  private boolean change(
      ServerWorld world, BlockPos pos, java.util.function.UnaryOperator<TerminalSettings> edit) {
    final TerminalBlockEntity terminal = TerminalLookup.at(world, pos).orElse(null);
    if (terminal == null) {
      return false;
    }
    final TerminalSettings before = terminal.settings();
    final TerminalSettings after = edit.apply(before);
    if (after.equals(before)) {
      return false;
    }
    terminal.setSettings(after);
    access.revokeLostAccess(world, pos);
    return true;
  }
}
