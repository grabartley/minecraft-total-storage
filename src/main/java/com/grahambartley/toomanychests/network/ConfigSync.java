package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Keeps each connected client's copy of the server configuration in step with the server's own.
 *
 * <p>What a client holds is for drawing screens and predicting keybinds. Nothing here reads back
 * from a client to decide anything: a rejected edit is answered with the true configuration, so a
 * client that tried to change a setting it may not change ends up displaying reality rather than
 * what it asked for.
 */
public final class ConfigSync {
  private ConfigSync() {}

  static void register() {
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendTo(handler.player));
    ServerConfigChanges.subscribe(ConfigSync::broadcast);
  }

  /** Sends the live configuration to one player, skipping clients that cannot receive it. */
  public static void sendTo(ServerPlayerEntity player) {
    send(player, ServerConfigService.current());
  }

  public static void broadcast(MinecraftServer server, TooManyChestsConfig config) {
    if (server == null || config == null) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      send(player, config);
    }
  }

  /**
   * Adopts an edit only from a sender who still holds operator permission at the moment it lands,
   * rather than at the moment the screen that sent it was opened.
   */
  public static void handleEdit(ServerPlayerEntity player, TooManyChestsConfig requested) {
    if (player == null) {
      return;
    }
    if (requested == null
        || !player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL)
        || !ServerConfigService.update(player.getServer(), requested)) {
      sendTo(player);
    }
  }

  private static void send(ServerPlayerEntity player, TooManyChestsConfig config) {
    if (player == null || !ServerPlayNetworking.canSend(player, ConfigSyncPayload.ID)) {
      return;
    }
    ServerPlayNetworking.send(player, new ConfigSyncPayload(config));
  }
}
