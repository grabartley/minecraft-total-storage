package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.screenhandler.TerminalActions;
import com.grahambartley.toomanychests.screenhandler.TerminalListingService;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsEdits;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsService;
import com.grahambartley.toomanychests.screenhandler.TerminalTagEdits;
import com.grahambartley.toomanychests.screenhandler.TerminalTagPanelService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class TerminalSettingsNetworking {
  private static final TerminalSettingsService PANELS = new TerminalSettingsService();
  private static final TerminalSettingsEdits EDITS = new TerminalSettingsEdits(PANELS);
  private static final TerminalActions ACTIONS = new TerminalActions(EDITS);
  private static final TerminalTagPanelService TAG_PANELS = new TerminalTagPanelService();
  private static final TerminalTagEdits TAG_EDITS =
      new TerminalTagEdits(TAG_PANELS, new TerminalListingService());

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
    PayloadTypeRegistry.playC2S().register(TerminalActionPayload.ID, TerminalActionPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(TagPanelRequestPayload.ID, TagPanelRequestPayload.CODEC);
    PayloadTypeRegistry.playC2S().register(TagEditPayload.ID, TagEditPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(TagPanelPayload.ID, TagPanelPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(NetworkSettingsPayload.ID, NetworkSettingsPayload.CODEC);
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
        ContainerRoleEditPayload.ID,
        (payload, context) ->
            context.server().execute(() -> EDITS.setRole(context.player(), payload)));
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
  }
}
