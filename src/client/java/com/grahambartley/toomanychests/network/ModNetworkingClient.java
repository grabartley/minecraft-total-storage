package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.config.ClientConfigMirror;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.filter.TagCombinationMode;
import com.grahambartley.toomanychests.listing.ListingWindow;
import com.grahambartley.toomanychests.listing.TerminalListingMirror;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.render.ContainerHighlights;
import com.grahambartley.toomanychests.screen.NetworkSettingsMirror;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import java.util.Set;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;

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
    ClientPlayNetworking.registerGlobalReceiver(
        NetworkSettingsPayload.ID,
        (payload, context) ->
            context
                .client()
                .execute(() -> NetworkSettingsMirror.accept(payload.syncId(), payload.view())));
    ClientPlayNetworking.registerGlobalReceiver(
        ContainerHighlightPayload.ID,
        (payload, context) ->
            context
                .client()
                .execute(() -> ContainerHighlights.show(payload.containers(), payload.ticks())));
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> {
          ClientConfigMirror.clear();
          TerminalListingMirror.closed();
          NetworkSettingsMirror.closed();
          ContainerHighlights.clear();
        });
  }

  public static void sendLockToggle(int slot) {
    ClientPlayNetworking.send(new LockTogglePayload(slot));
  }

  public static void sendConfigEdit(TooManyChestsConfig config) {
    ClientPlayNetworking.send(new ConfigEditPayload(config));
  }

  /**
   * Asks the network for some of an item. Per ADR 0009 the request names the item and the amount
   * and never where the client thinks it is, so nothing here can be lied about into a duplication.
   */
  public static void sendWithdrawRequest(
      int syncId, ItemVariant variant, int amount, WithdrawDestination destination) {
    ClientPlayNetworking.send(new WithdrawRequestPayload(syncId, variant, amount, destination));
  }

  /** Offers the network what the player is holding, or one slot of what they are carrying. */
  public static void sendDepositRequest(DepositRequestPayload request) {
    ClientPlayNetworking.send(request);
  }

  /** Offers the network everything the server decides is eligible, which this end never lists. */
  public static void sendDepositAllRequest(int syncId) {
    ClientPlayNetworking.send(new DepositAllRequestPayload(syncId));
  }

  /** Asks for the settings panel, and for the slice of the container list it is about to show. */
  public static void sendSettingsRequest(int syncId, int offset, int length) {
    ClientPlayNetworking.send(new NetworkSettingsRequestPayload(syncId, offset, length));
  }

  public static void sendRadiusEdit(int syncId, int blocks) {
    ClientPlayNetworking.send(new TerminalRadiusEditPayload(syncId, blocks));
  }

  public static void sendAccessEdit(int syncId, TerminalAccessMode mode) {
    ClientPlayNetworking.send(new TerminalAccessEditPayload(syncId, mode));
  }

  public static void sendRoleEdit(int syncId, BlockPos container, ContainerRole role) {
    ClientPlayNetworking.send(new ContainerRoleEditPayload(syncId, container, role));
  }

  public static void sendTerminalAction(int syncId, TerminalAction action) {
    ClientPlayNetworking.send(new TerminalActionPayload(syncId, action));
  }

  /** Asks for the rows a screen is about to draw, and for the criteria it is drawing them under. */
  public static void sendListingRequest(
      int syncId, ListingWindow window, String search, TagCombinationMode mode, Set<String> tags) {
    ClientPlayNetworking.send(
        new ListingRequestPayload(syncId, window.offset(), window.length(), search, mode, tags));
  }
}
