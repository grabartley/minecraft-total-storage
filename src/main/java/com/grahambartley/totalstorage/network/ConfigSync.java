package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.access.ServerSettingsAccess;
import com.grahambartley.totalstorage.config.ServerConfigChanges;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
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

  public static void broadcast(MinecraftServer server, TotalStorageConfig config) {
    if (server == null || config == null) {
      return;
    }
    for (final ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
      send(player, config);
    }
  }

  public static void handleEdit(ServerPlayerEntity player, TotalStorageConfig requested) {
    if (player == null) {
      return;
    }
    if (requested == null
        || !ServerSettingsAccess.mayEdit(player)
        || !ServerConfigService.update(player.getServer(), requested)) {
      sendTo(player);
    }
  }

  private static void send(ServerPlayerEntity player, TotalStorageConfig config) {
    if (player == null || !ServerPlayNetworking.canSend(player, ConfigSyncPayload.ID)) {
      return;
    }
    ServerPlayNetworking.send(player, new ConfigSyncPayload(config));
  }
}
