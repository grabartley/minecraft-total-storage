package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.network.TerminalNoticePayload;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class TerminalNotice {
  private TerminalNotice() {}

  public static void tell(ServerPlayerEntity player, Text message) {
    send(player, message, false);
  }

  public static void ask(ServerPlayerEntity player, Text message) {
    send(player, message, true);
  }

  private static void send(ServerPlayerEntity player, Text message, boolean needsAnAnswer) {
    if (player == null || message == null) {
      return;
    }
    if (!(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || !ServerPlayNetworking.canSend(player, TerminalNoticePayload.ID)) {
      player.sendMessage(message, true);
      return;
    }
    ServerPlayNetworking.send(
        player, new TerminalNoticePayload(handler.syncId, message, needsAnAnswer));
  }
}
