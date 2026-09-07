package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.listing.TerminalListingMirror;
import java.util.Set;
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
    ClientPlayNetworking.registerGlobalReceiver(
        ListingPayload.ID,
        (payload, context) ->
            context
                .client()
                .execute(() -> TerminalListingMirror.accept(payload.syncId(), payload.page())));
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> {
          ClientConfigMirror.clear();
          TerminalListingMirror.closed();
        });
  }

  public static void sendLockToggle(int slot) {
    ClientPlayNetworking.send(new LockTogglePayload(slot));
  }

  public static void sendConfigEdit(TooManyChestsConfig config) {
    ClientPlayNetworking.send(new ConfigEditPayload(config));
  }

  /** Asks for the rows a screen is about to draw, and for the criteria it is drawing them under. */
  public static void sendListingRequest(
      int syncId, ListingWindow window, String search, TagCombinationMode mode, Set<String> tags) {
    ClientPlayNetworking.send(
        new ListingRequestPayload(syncId, window.offset(), window.length(), search, mode, tags));
  }
}
