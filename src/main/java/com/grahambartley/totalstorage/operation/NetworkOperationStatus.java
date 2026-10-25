package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.network.NetworkOperationStatusPayload;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.screenhandler.TerminalViewerSync;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NetworkOperationStatus {
  public record Delivery(ServerPlayerEntity player, NetworkOperationStatusPayload payload) {}

  private NetworkOperationStatus() {}

  public static void armed(ServerPlayerEntity player, ExpensiveOperation operation) {
    deliver(armedDeliveries(player, operation));
  }

  public static void running(
      MinecraftServer server, TerminalAddress terminal, ExpensiveOperation operation, int percent) {
    deliver(runningDeliveries(server, terminal, operation, percent));
  }

  public static void idle(
      MinecraftServer server, TerminalAddress terminal, ExpensiveOperation operation) {
    deliver(idleDeliveries(server, terminal, operation));
  }

  public static List<Delivery> armedDeliveries(
      ServerPlayerEntity player, ExpensiveOperation operation) {
    return deliveryFor(player, operation, NetworkOperationPhase.ARMED, 0).stream().toList();
  }

  public static List<Delivery> runningDeliveries(
      MinecraftServer server, TerminalAddress terminal, ExpensiveOperation operation, int percent) {
    return toViewers(server, terminal, operation, NetworkOperationPhase.RUNNING, percent);
  }

  public static List<Delivery> idleDeliveries(
      MinecraftServer server, TerminalAddress terminal, ExpensiveOperation operation) {
    return toViewers(server, terminal, operation, NetworkOperationPhase.IDLE, 0);
  }

  private static List<Delivery> toViewers(
      MinecraftServer server,
      TerminalAddress terminal,
      ExpensiveOperation operation,
      NetworkOperationPhase phase,
      int percent) {
    return TerminalViewerSync.playersOn(server, terminal).stream()
        .flatMap(viewer -> deliveryFor(viewer, operation, phase, percent).stream())
        .toList();
  }

  private static Optional<Delivery> deliveryFor(
      ServerPlayerEntity player,
      ExpensiveOperation operation,
      NetworkOperationPhase phase,
      int percent) {
    if (player == null || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)) {
      return Optional.empty();
    }
    return Optional.of(
        new Delivery(
            player, new NetworkOperationStatusPayload(handler.syncId, operation, phase, percent)));
  }

  private static void deliver(List<Delivery> deliveries) {
    for (final Delivery delivery : deliveries) {
      if (ServerPlayNetworking.canSend(delivery.player(), NetworkOperationStatusPayload.ID)) {
        ServerPlayNetworking.send(delivery.player(), delivery.payload());
      }
    }
  }
}
