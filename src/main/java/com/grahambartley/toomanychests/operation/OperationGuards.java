package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.config.ServerConfigService;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * The one place an expensive operation asks whether it may run yet.
 *
 * <p>Wires {@link OperationCooldowns}, which knows only arithmetic, to the three things it must not
 * know: the server's clock, the operator's configured cooldown, and what a refused player is told.
 * Keeping those out of the cooldown is what lets its boundaries be tested without a world.
 *
 * <p>A refusal says so. A limit that silently drops a click is indistinguishable from a mod that
 * has stopped working, which is the reading a player reaches for first and the one hardest to talk
 * them out of.
 */
public final class OperationGuards {
  private static final OperationCooldowns COOLDOWNS = new OperationCooldowns();

  private OperationGuards() {}

  public static OperationCooldowns cooldowns() {
    return COOLDOWNS;
  }

  /** A player who has left cannot be on cooldown, and their row would outlive their session. */
  public static void register() {
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> COOLDOWNS.forget(handler.getPlayer().getUuid()));
  }

  /**
   * Whether this player may run this operation now, spending the cooldown when they may.
   *
   * <p>Returns what to tell them when they may not, so a caller cannot take the refusal and forget
   * to say anything. An empty answer is permission.
   */
  public static Optional<Text> refuse(ServerPlayerEntity player, ExpensiveOperation operation) {
    if (player == null || player.getServer() == null) {
      return Optional.empty();
    }
    final int cooldown = ServerConfigService.current().expensiveOperationCooldownTicks();
    final long tick = player.getServer().getTicks();
    if (COOLDOWNS.claim(player.getUuid(), operation, tick, cooldown)) {
      return Optional.empty();
    }
    return Optional.of(
        OperationMessages.rateLimited(
            operation, COOLDOWNS.remaining(player.getUuid(), operation, tick, cooldown)));
  }

  /** Refuses and tells the player in one step, for a caller with nothing else to add. */
  public static boolean allows(ServerPlayerEntity player, ExpensiveOperation operation) {
    final Optional<Text> refusal = refuse(player, operation);
    refusal.ifPresent(message -> player.sendMessage(message, true));
    return refusal.isEmpty();
  }
}
