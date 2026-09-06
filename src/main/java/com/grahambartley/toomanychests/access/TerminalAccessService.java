package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalMessages;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The gate every terminal operation goes through.
 *
 * <p>Reading the settings fresh on each call rather than caching a decision is what makes ADR
 * 0008's per-operation check real: a trust list edited a tick ago binds the next click, and there
 * is no window in which a stale answer is still being honoured.
 *
 * <p>A position holding no terminal is refused rather than allowed, so a terminal broken out from
 * under an open screen fails closed.
 */
public final class TerminalAccessService {
  private static final Logger LOGGER = LoggerFactory.getLogger(TerminalAccessService.class);

  private final TerminalViewers viewers;

  public TerminalAccessService(TerminalViewers viewers) {
    this.viewers = Objects.requireNonNull(viewers, "viewers");
  }

  public TerminalViewers viewers() {
    return viewers;
  }

  /** The decision on its own, which is what an operation already under way asks for. */
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

  /**
   * The decision for a player asking to open the terminal, which is the one moment an operator
   * reaching past a lock is worth a line in the server log. ADR 0008 accepts the bypass and makes
   * logging it the mitigation, so it is recorded here rather than left to each caller.
   */
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

  /**
   * Shuts anyone who has just lost access out of the screen they still have open.
   *
   * <p>Called after every edit to a terminal's access settings. Without it a revoked player keeps a
   * screen that refuses each click, which reads as a broken mod rather than as a lock.
   */
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

  /**
   * Whether a player holds the operator level the moderation bypass is granted at.
   *
   * <p>Asked of the server's operator list rather than of the entity, because an entity's own
   * permission level is the one a command block or a mob carries and is zero for every player,
   * including the host of a single player world.
   */
  public static boolean isOperator(PlayerEntity player) {
    if (player == null || player.getServer() == null) {
      return false;
    }
    return player.getServer().getPermissionLevel(player.getGameProfile())
        >= ServerConfigService.OP_PERMISSION_LEVEL;
  }
}
