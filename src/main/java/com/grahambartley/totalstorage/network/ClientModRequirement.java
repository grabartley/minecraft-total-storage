package com.grahambartley.totalstorage.network;

import com.grahambartley.totalstorage.TotalStorage;
import java.util.Optional;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.minecraft.text.Text;

public final class ClientModRequirement {
  private ClientModRequirement() {}

  public static void register() {
    PayloadTypeRegistry.configurationS2C()
        .register(ClientModPresencePayload.ID, ClientModPresencePayload.CODEC);
    ServerConfigurationConnectionEvents.CONFIGURE.register(
        (handler, server) ->
            refusalFor(ServerConfigurationNetworking.canSend(handler, ClientModPresencePayload.ID))
                .ifPresent(handler::disconnect));
  }

  public static Optional<Text> refusalFor(boolean clientHasTheMod) {
    return clientHasTheMod ? Optional.empty() : Optional.of(refusal());
  }

  private static Text refusal() {
    return Text.translatableWithFallback(
        "multiplayer." + TotalStorage.MOD_ID + ".client_mod_required",
        "This server runs Total Storage. Install the same mod on your game to join.");
  }
}
