package com.grahambartley.toomanychests.network;

import com.grahambartley.toomanychests.lock.LockToggleService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers the mod's packets and the server-side handlers that resolve them.
 *
 * <p>Payload types register here rather than in the client initialiser because both sides must
 * agree on the codec before a channel is usable, and a client running this same entrypoint gets the
 * registration for free.
 */
public final class ModNetworking {
  private ModNetworking() {}

  public static void register() {
    PayloadTypeRegistry.playC2S().register(LockTogglePayload.ID, LockTogglePayload.CODEC);
    PayloadTypeRegistry.playC2S().register(ConfigEditPayload.ID, ConfigEditPayload.CODEC);
    PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);

    ServerPlayNetworking.registerGlobalReceiver(
        LockTogglePayload.ID,
        (payload, context) -> LockToggleService.toggle(context.player(), payload.slot()));
    ServerPlayNetworking.registerGlobalReceiver(
        ConfigEditPayload.ID,
        (payload, context) ->
            context
                .server()
                .execute(() -> ConfigSync.handleEdit(context.player(), payload.config())));

    ConfigSync.register();
  }
}
