package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.network.NetworkOperationStatusPayload;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.screenhandler.TerminalViewerSync;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NetworkOperationStatus {
  private NetworkOperationStatus() {}

  public static void armed(ServerPlayerEntity player, ExpensiveOperation operation) {
    send(player, operation, NetworkOperationPhase.ARMED, 0);
  }

  public static void running(
      MinecraftServer server, TerminalAddress terminal, ExpensiveOperation operation, int percent) {
    TerminalViewerSync.playersOn(server, terminal)
        .forEach(viewer -> send(viewer, operation, NetworkOperationPhase.RUNNING, percent));
  }

  public static void idle(
      MinecraftServer server, TerminalAddress terminal, ExpensiveOperation operation) {
    TerminalViewerSync.playersOn(server, terminal)
        .forEach(viewer -> send(viewer, operation, NetworkOperationPhase.IDLE, 0));
  }

  private static void send(
      ServerPlayerEntity player,
      ExpensiveOperation operation,
      NetworkOperationPhase phase,
      int percent) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || !ServerPlayNetworking.canSend(player, NetworkOperationStatusPayload.ID)) {
      return;
    }
    ServerPlayNetworking.send(
        player, new NetworkOperationStatusPayload(handler.syncId, operation, phase, percent));
  }
}
