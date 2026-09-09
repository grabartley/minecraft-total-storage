package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.network.ModNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalResync {
  private TerminalResync() {}

  public static void of(ServerPlayerEntity player) {
    if (player == null) {
      return;
    }
    ModNetworking.listings().refresh(player);
    if (player.currentScreenHandler != null) {
      player.currentScreenHandler.syncState();
    }
  }
}
