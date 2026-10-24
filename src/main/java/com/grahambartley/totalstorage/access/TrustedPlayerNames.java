package com.grahambartley.totalstorage.access;

import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TrustedPlayerNames {
  private TrustedPlayerNames() {}

  public static String nameOf(MinecraftServer server, UUID player) {
    if (server == null || server.getUserCache() == null) {
      return player.toString();
    }
    return server
        .getUserCache()
        .getByUuid(player)
        .map(GameProfile::getName)
        .orElseGet(player::toString);
  }

  public static Optional<UUID> find(MinecraftServer server, String name) {
    if (server == null || name == null || name.isBlank()) {
      return Optional.empty();
    }
    final ServerPlayerEntity online = server.getPlayerManager().getPlayer(name);
    if (online != null) {
      return Optional.of(online.getUuid());
    }
    if (server.getUserCache() == null) {
      return Optional.empty();
    }
    return server.getUserCache().findByName(name).map(GameProfile::getId);
  }
}
