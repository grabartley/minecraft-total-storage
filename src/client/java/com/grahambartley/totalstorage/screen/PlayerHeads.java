package com.grahambartley.totalstorage.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Uuids;

public final class PlayerHeads {
  private PlayerHeads() {}

  public static void draw(DrawContext context, String player, int x, int y, int size) {
    PlayerSkinDrawer.draw(context, skinOf(MinecraftClient.getInstance(), player), x, y, size);
  }

  private static SkinTextures skinOf(MinecraftClient client, String player) {
    if (client.getNetworkHandler() != null) {
      final PlayerListEntry online = client.getNetworkHandler().getPlayerListEntry(player);
      if (online != null) {
        return online.getSkinTextures();
      }
    }
    return DefaultSkinHelper.getSkinTextures(Uuids.getOfflinePlayerUuid(player));
  }
}
