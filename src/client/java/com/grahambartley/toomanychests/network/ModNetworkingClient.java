package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Sends the mod's client-to-server requests and files what the server sends back. */
public final class ModNetworkingClient {
  private ModNetworkingClient() {}

  public static void register() {
    ClientPlayNetworking.registerGlobalReceiver(
        ConfigSyncPayload.ID,
        (payload, context) ->
            context.client().execute(() -> ClientConfigMirror.accept(payload.config())));
    ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ClientConfigMirror.clear());
  }

  public static void sendLockToggle(int slot) {
    ClientPlayNetworking.send(new LockTogglePayload(slot));
  }

  public static void sendConfigEdit(TooManyChestsConfig config) {
    ClientPlayNetworking.send(new ConfigEditPayload(config));
  }
}
