package com.grahambartley.toomanychests.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/** Sends the mod's client-to-server requests. */
public final class ModNetworkingClient {
  private ModNetworkingClient() {}

  public static void sendLockToggle(int slot) {
    ClientPlayNetworking.send(new LockTogglePayload(slot));
  }
}
