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
import com.grahambartley.toomanychests.screen.NetworkTagMirror;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import java.util.Set;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;

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
    ClientPlayNetworking.registerGlobalReceiver(
        TagPanelPayload.ID,
        (payload, context) ->
            context
                .client()
                .execute(() -> NetworkTagMirror.accept(payload.syncId(), payload.view())));
    ClientPlayConnectionEvents.DISCONNECT.register(
        (handler, client) -> {
          ClientConfigMirror.clear();
          TerminalListingMirror.closed();
          NetworkSettingsMirror.closed();
          NetworkTagMirror.closed();
          ContainerHighlights.clear();
        });
  }

  public static void sendLockToggle(int slot) {
    ClientPlayNetworking.send(new LockTogglePayload(slot));
  }

  public static void sendConfigEdit(TooManyChestsConfig config) {
    ClientPlayNetworking.send(new ConfigEditPayload(config));
  }

  public static void sendWithdrawRequest(
      int syncId, ItemVariant variant, int amount, WithdrawDestination destination) {
    ClientPlayNetworking.send(new WithdrawRequestPayload(syncId, variant, amount, destination));
  }

  public static void sendDepositRequest(DepositRequestPayload request) {
    ClientPlayNetworking.send(request);
  }

  public static void sendQuickStackRequest() {
    ClientPlayNetworking.send(new QuickStackRequestPayload());
  }

  public static void sendDepositAllRequest(int syncId) {
    ClientPlayNetworking.send(new DepositAllRequestPayload(syncId));
  }

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

  public static void sendTagPanelRequest(int syncId, boolean open, ItemVariant subject) {
    ClientPlayNetworking.send(
        open
            ? TagPanelRequestPayload.forItem(syncId, subject)
            : TagPanelRequestPayload.closed(syncId));
  }

  public static void sendTagCreate(int syncId, String name) {
    ClientPlayNetworking.send(TagEditPayload.create(syncId, name));
  }

  public static void sendTagRename(int syncId, String tagId, String name) {
    ClientPlayNetworking.send(TagEditPayload.rename(syncId, tagId, name));
  }

  public static void sendTagDelete(int syncId, String tagId) {
    ClientPlayNetworking.send(TagEditPayload.delete(syncId, tagId));
  }

  public static void sendTagAssign(int syncId, String tagId, ItemVariant item, boolean assigned) {
    ClientPlayNetworking.send(TagEditPayload.assign(syncId, tagId, item, assigned));
  }

  public static void sendTagAssignMatching(int syncId, String tagId) {
    ClientPlayNetworking.send(TagEditPayload.assignMatching(syncId, tagId));
  }

  public static void sendListingRequest(
      int syncId, ListingWindow window, String search, TagCombinationMode mode, Set<String> tags) {
    ClientPlayNetworking.send(
        new ListingRequestPayload(syncId, window.offset(), window.length(), search, mode, tags));
  }
}
