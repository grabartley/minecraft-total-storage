package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.screenhandler.TerminalSettingsEdits;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * The settings panel's own channel: the request that draws it, the edits it sends, and the answers
 * that come back.
 *
 * <p>Registered apart from the rest so {@link ModNetworking} stays a list of what the mod speaks
 * rather than a list of everything it does. Every receiver here hops onto the server thread first,
 * because each of them reads or writes a block entity.
 */
public final class TerminalSettingsNetworking {
  private static final TerminalSettingsService PANELS = new TerminalSettingsService();
  private static final TerminalSettingsEdits EDITS = new TerminalSettingsEdits(PANELS);

  private TerminalSettingsNetworking() {}

  /** The one panel service, so whatever pushes an update answers with the same one. */
  public static TerminalSettingsService panels() {
    return PANELS;
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
        (payload, context) -> context.server().execute(() -> EDITS.act(context.player(), payload)));
  }
}
