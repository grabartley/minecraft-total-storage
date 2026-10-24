package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.network.ModNetworking;
import com.grahambartley.totalstorage.network.TerminalSettingsNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalResync {
  private TerminalResync() {}

  public static void of(ServerPlayerEntity player) {
    if (player == null) {
      return;
    }
    contents(player);
    if (player.currentScreenHandler != null) {
      player.currentScreenHandler.syncState();
    }
  }

  public static void contents(ServerPlayerEntity player) {
    ModNetworking.listings().refresh(player);
    TerminalSettingsNetworking.tagPanels().contentsChanged(player);
  }
}
