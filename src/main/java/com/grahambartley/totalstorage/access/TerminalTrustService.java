package com.grahambartley.totalstorage.access;

import com.grahambartley.totalstorage.block.entity.TerminalBlockEntity;
import com.grahambartley.totalstorage.config.TerminalAccessMode;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TerminalTrustService {
  private final TerminalAccessService access;

  public TerminalTrustService(TerminalAccessService access) {
    this.access = Objects.requireNonNull(access, "access");
  }

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

  public TrustEditOutcome trustNamed(ServerWorld world, BlockPos pos, String name) {
    final UUID player = TrustedPlayerNames.find(world.getServer(), name).orElse(null);
    if (player == null) {
      return TrustEditOutcome.UNKNOWN_PLAYER;
    }
    return trust(world, pos, player) ? TrustEditOutcome.ADDED : TrustEditOutcome.ALREADY_TRUSTED;
  }

  public TrustEditOutcome untrustNamed(ServerWorld world, BlockPos pos, String name) {
    final TerminalSettings settings = settingsAt(world, pos).orElse(null);
    if (settings == null || name == null) {
      return TrustEditOutcome.NOT_TRUSTED;
    }
    for (final UUID trusted : settings.trusted()) {
      if (TrustedPlayerNames.nameOf(world.getServer(), trusted).equalsIgnoreCase(name)) {
        return untrust(world, pos, trusted)
            ? TrustEditOutcome.REMOVED
            : TrustEditOutcome.NOT_TRUSTED;
      }
    }
    return TrustEditOutcome.NOT_TRUSTED;
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
