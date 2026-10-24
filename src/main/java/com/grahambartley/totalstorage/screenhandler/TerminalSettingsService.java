package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.network.NetworkSettingsPayload;
import com.grahambartley.totalstorage.network.NetworkSettingsRequestPayload;
import com.grahambartley.totalstorage.operation.TerminalOperationContext;
import com.grahambartley.totalstorage.operation.TerminalOperationGate;
import com.grahambartley.totalstorage.terminal.NetworkSettingsService;
import com.grahambartley.totalstorage.terminal.NetworkSettingsView;
import java.util.Objects;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalSettingsService {
  private final NetworkSettingsService settings;

  public TerminalSettingsService() {
    this(new NetworkSettingsService());
  }

  public TerminalSettingsService(NetworkSettingsService settings) {
    this.settings = Objects.requireNonNull(settings, "settings");
  }

  public boolean request(ServerPlayerEntity player, NetworkSettingsRequestPayload payload) {
    if (payload == null) {
      return false;
    }
    if (!payload.showing()) {
      return closed(player, payload.syncId());
    }
    final TerminalOperationContext context =
        TerminalOperationGate.open(player, payload.syncId()).orElse(null);
    if (context == null) {
      return false;
    }
    context.handler().settings().showing(payload.window());
    send(player, context.handler());
    return true;
  }

  private static boolean closed(ServerPlayerEntity player, int syncId) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || handler.syncId != syncId) {
      return false;
    }
    handler.settings().closed();
    return true;
  }

  public boolean refresh(ServerPlayerEntity player) {
    if (player == null
        || !(player.currentScreenHandler instanceof TerminalScreenHandler handler)
        || !handler.settings().isOpen()) {
      return false;
    }
    send(player, handler);
    return true;
  }

  public NetworkSettingsView viewFor(ServerPlayerEntity player, TerminalScreenHandler handler) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(handler, "handler");
    return settings.viewFor(player, handler.terminal(), handler.settings().window());
  }

  private void send(ServerPlayerEntity player, TerminalScreenHandler handler) {
    ServerPlayNetworking.send(
        player, new NetworkSettingsPayload(handler.syncId, viewFor(player, handler)));
  }
}
