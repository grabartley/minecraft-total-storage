package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.network.TerminalNoticePayload;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class TerminalNotice {
  private TerminalNotice() {}

  public static void done(ServerPlayerEntity player, Text message) {
    send(player, TerminalNoticeTone.DONE, message, message);
  }

  public static void partial(ServerPlayerEntity player, Text message) {
    partial(player, message, message);
  }

  public static void partial(ServerPlayerEntity player, Text headline, Text message) {
    send(player, TerminalNoticeTone.PARTIAL, headline, message);
  }

  public static void failed(ServerPlayerEntity player, Text message) {
    failed(player, message, message);
  }

  public static void failed(ServerPlayerEntity player, Text headline, Text message) {
    send(player, TerminalNoticeTone.FAILED, headline, message);
  }

  public static void report(
      ServerPlayerEntity player, TerminalNoticeTone tone, Text headline, Text message) {
    send(player, tone, headline, message);
  }

  private static void send(
      ServerPlayerEntity player, TerminalNoticeTone tone, Text headline, Text message) {
    if (player == null || message == null) {
      return;
    }
    if (!(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || !ServerPlayNetworking.canSend(player, TerminalNoticePayload.ID)) {
      player.sendMessage(message, true);
      return;
    }
    if (tone.isShownInTheTerminal()) {
      ServerPlayNetworking.send(
          player, new TerminalNoticePayload(handler.syncId, tone, headline, message));
    }
  }
}
