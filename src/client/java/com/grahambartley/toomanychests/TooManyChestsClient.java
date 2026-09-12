package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.render.ContainerHighlightRenderer;
import com.grahambartley.toomanychests.screen.InventoryQuickStackButton;
import com.grahambartley.toomanychests.screen.ModKeyBindings;
import com.grahambartley.toomanychests.screen.TerminalScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class TooManyChestsClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ModNetworkingClient.register();
    ContainerHighlightRenderer.register();
    ModKeyBindings.register();
    InventoryQuickStackButton.register();
    HandledScreens.register(ModScreenHandlers.TERMINAL, TerminalScreen::new);
    ClientTickEvents.END_CLIENT_TICK.register(TooManyChestsClient::pollQuickStack);
  }

  private static void pollQuickStack(net.minecraft.client.MinecraftClient client) {
    while (ModKeyBindings.quickStack() != null && ModKeyBindings.quickStack().wasPressed()) {
      ModNetworkingClient.sendQuickStackRequest();
    }
  }
}
