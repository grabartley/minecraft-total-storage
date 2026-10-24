package com.grahambartley.totalstorage.access;

import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalMessages;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TerminalAccessService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TerminalAccessService.class);

  private final TerminalViewers viewers;

  public TerminalAccessService(TerminalViewers viewers) {
    this.viewers = Objects.requireNonNull(viewers, "viewers");
  }

  public TerminalViewers viewers() {
    return viewers;
  }

  public TerminalAccess decide(ServerWorld world, BlockPos pos, PlayerEntity player) {
    if (player == null) {
      return TerminalAccess.DENIED;
    }
    return TerminalLookup.settingsAt(world, pos)
        .map(settings -> TerminalAccessRules.decide(settings, player.getUuid(), isOperator(player)))
        .orElse(TerminalAccess.DENIED);
  }

  public boolean allows(ServerWorld world, BlockPos pos, PlayerEntity player) {
    return decide(world, pos, player).allowed();
  }

  public TerminalAccess open(ServerWorld world, BlockPos pos, PlayerEntity player) {
    final TerminalAccess decision = decide(world, pos, player);
    if (decision.viaOperator()) {
      LOGGER.info(
          "{} opened the terminal at {} through the operator bypass",
          player.getName().getString(),
          TerminalAddress.of(world, pos).describe());
    }
    return decision;
  }

  public void revokeLostAccess(ServerWorld world, BlockPos pos) {
    if (world == null || pos == null || world.getServer() == null) {
      return;
    }
    final TerminalAddress terminal = TerminalAddress.of(world, pos);
    for (final UUID viewer : viewers.viewing(terminal)) {
      final ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(viewer);
      if (player == null) {
        viewers.closed(terminal, viewer);
      } else if (!allows(world, pos, player)) {
        player.closeHandledScreen();
        viewers.closed(terminal, viewer);
        player.sendMessage(TerminalMessages.accessRevoked(), true);
      }
    }
  }

  public static boolean isOperator(PlayerEntity player) {
    if (player == null || player.getServer() == null) {
      return false;
    }
    return player.getServer().getPermissionLevel(player.getGameProfile())
        >= ServerConfigService.OP_PERMISSION_LEVEL;
  }
}
