package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.network.ModNetworking;
import com.grahambartley.toomanychests.network.TerminalSettingsNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalResync {
  private TerminalResync() {}

  public static void of(ServerPlayerEntity player) {
    if (player == null) {
      return;
    }
    ModNetworking.listings().refresh(player);
    TerminalSettingsNetworking.tagPanels().contentsChanged(player);
    if (player.currentScreenHandler != null) {
      player.currentScreenHandler.syncState();
    }
  }
}
