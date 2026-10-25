package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.config.ServerConfigService;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class OperationGuards {
  private static final OperationCooldowns COOLDOWNS = new OperationCooldowns();

  private OperationGuards() {}

  public static OperationCooldowns cooldowns() {
    return COOLDOWNS;
  }

  public static void register() {
    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> COOLDOWNS.forget(handler.getPlayer().getUuid()));
  }

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

  public static boolean allows(ServerPlayerEntity player, ExpensiveOperation operation) {
    final Optional<Text> refusal = refuse(player, operation);
    refusal.ifPresent(message -> TerminalNotice.partial(player, message));
    return refusal.isEmpty();
  }
}
