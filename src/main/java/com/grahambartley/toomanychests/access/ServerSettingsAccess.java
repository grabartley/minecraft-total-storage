package com.grahambartley.toomanychests.access;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class ServerSettingsAccess {
  private ServerSettingsAccess() {}

  public static boolean mayEdit(@Nullable ServerCommandSource source) {
    if (source == null) {
      return false;
    }
    return decide(
        source.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL),
        source.getServer(),
        source.getPlayer());
  }

  public static boolean mayEdit(@Nullable ServerPlayerEntity player) {
    if (player == null) {
      return false;
    }
    return decide(
        player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL),
        player.getServer(),
        player);
  }

  private static boolean decide(
      boolean operator, @Nullable MinecraftServer server, @Nullable ServerPlayerEntity player) {
    if (server == null || player == null) {
      return operator;
    }
    final GameProfile profile = player.getGameProfile();
    return ServerSettingsAccessRule.mayEdit(
        operator, server.isDedicated(), profile != null && server.isHost(profile));
  }
}
