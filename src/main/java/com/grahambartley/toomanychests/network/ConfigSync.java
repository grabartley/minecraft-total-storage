package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.access.ServerSettingsAccess;
import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ConfigSync {
  private ConfigSync() {}

  static void register() {
    ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sendTo(handler.player));
    ServerConfigChanges.subscribe(ConfigSync::broadcast);
  }

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

  public static void handleEdit(ServerPlayerEntity player, TooManyChestsConfig requested) {
    if (player == null) {
      return;
    }
    if (requested == null
        || !ServerSettingsAccess.mayEdit(player)
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
