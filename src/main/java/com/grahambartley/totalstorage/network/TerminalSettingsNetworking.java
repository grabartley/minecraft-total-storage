package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.screenhandler.TerminalActions;
import com.grahambartley.totalstorage.screenhandler.TerminalListingService;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsEdits;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsService;
import com.grahambartley.totalstorage.screenhandler.TerminalTagEdits;
import com.grahambartley.totalstorage.screenhandler.TerminalTagPanelService;
import com.grahambartley.totalstorage.screenhandler.TerminalTagTransfers;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class TerminalSettingsNetworking {
  private static final TerminalSettingsService PANELS = new TerminalSettingsService();
  private static final TerminalSettingsEdits EDITS = new TerminalSettingsEdits(PANELS);
  private static final TerminalActions ACTIONS = new TerminalActions(EDITS);
  private static final TerminalTagPanelService TAG_PANELS = new TerminalTagPanelService();
  private static final TerminalListingService LISTINGS = new TerminalListingService();
  private static final TerminalTagEdits TAG_EDITS = new TerminalTagEdits(TAG_PANELS, LISTINGS);
  private static final TerminalTagTransfers TAG_TRANSFERS =
      new TerminalTagTransfers(TAG_PANELS, LISTINGS);

  private TerminalSettingsNetworking() {}

  public static TerminalSettingsService panels() {
    return PANELS;
  }

  public static TerminalTagPanelService tagPanels() {
    return TAG_PANELS;
  }

  public static void register() {
    PayloadTypeRegistry.playC2S()
        .register(NetworkSettingsRequestPayload.ID, NetworkSettingsRequestPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(TerminalRadiusEditPayload.ID, TerminalRadiusEditPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(TerminalAccessEditPayload.ID, TerminalAccessEditPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(ContainerRoleEditPayload.ID, ContainerRoleEditPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(ContainerNameEditPayload.ID, ContainerNameEditPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(ContainerFilterEditPayload.ID, ContainerFilterEditPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(ContainerLocatePayload.ID, ContainerLocatePayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(TerminalTrustEditPayload.ID, TerminalTrustEditPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(TerminalActionPayload.ID, TerminalActionPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(TagPanelRequestPayload.ID, TagPanelRequestPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(TagEditPayload.ID, TagEditPayload.CODEC);
    PayloadTypeRegistry.playC2S()
        .register(TagSourcesRequestPayload.ID, TagSourcesRequestPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(TagSetCopyPayload.ID, TagSetCopyPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(TagSourcesPayload.ID, TagSourcesPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(TagPanelPayload.ID, TagPanelPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(TerminalNoticePayload.ID, TerminalNoticePayload.CODEC);
    PayloadTypeRegistry.playS2C()
        .register(NetworkOperationStatusPayload.ID, NetworkOperationStatusPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(NetworkSettingsPayload.ID, NetworkSettingsPayload.CODEC);
    PayloadTypeRegistry.playS2C()
        .register(TerminalTrustReplyPayload.ID, TerminalTrustReplyPayload.CODEC);
    PayloadTypeRegistry.playS2C()
        .register(ContainerHighlightPayload.ID, ContainerHighlightPayload.CODEC);

    ServerPlayNetworking.registerGlobalReceiver(
        NetworkSettingsRequestPayload.ID,
        (payload, context) ->
            context.server().execute(() -> PANELS.request(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        TerminalRadiusEditPayload.ID,
        (payload, context) ->
            context.server().execute(() -> EDITS.setRadius(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        TerminalAccessEditPayload.ID,
        (payload, context) ->
            context.server().execute(() -> EDITS.setAccessMode(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        TerminalTrustEditPayload.ID,
        (payload, context) ->
            context.server().execute(() -> EDITS.setTrust(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        ContainerRoleEditPayload.ID,
        (payload, context) ->
            context.server().execute(() -> EDITS.setRole(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        ContainerNameEditPayload.ID,
        (payload, context) ->
            context.server().execute(() -> EDITS.setName(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        ContainerFilterEditPayload.ID,
        (payload, context) ->
            context.server().execute(() -> EDITS.setFilterTag(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        ContainerLocatePayload.ID,
        (payload, context) ->
            context.server().execute(() -> EDITS.locate(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        TerminalActionPayload.ID,
        (payload, context) ->
            context.server().execute(() -> ACTIONS.act(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        TagPanelRequestPayload.ID,
        (payload, context) ->
            context.server().execute(() -> TAG_PANELS.request(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        TagEditPayload.ID,
        (payload, context) ->
            context.server().execute(() -> TAG_EDITS.edit(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        TagSourcesRequestPayload.ID,
        (payload, context) ->
            context.server().execute(() -> TAG_TRANSFERS.listSources(context.player(), payload)));
    ServerPlayNetworking.registerGlobalReceiver(
        TagSetCopyPayload.ID,
        (payload, context) ->
            context.server().execute(() -> TAG_TRANSFERS.copy(context.player(), payload)));
  }
}
