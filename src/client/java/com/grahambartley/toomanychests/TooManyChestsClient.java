package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.render.ContainerHighlightRenderer;
import com.grahambartley.toomanychests.screen.ModKeyBindings;
import com.grahambartley.toomanychests.screen.ModScreenButtons;
import com.grahambartley.toomanychests.screen.QuickStackRequests;
import com.grahambartley.toomanychests.screen.TerminalScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;

public class TooManyChestsClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ClientPreferencesStore.load();
    ModNetworkingClient.register();
    ContainerHighlightRenderer.register();
    ModKeyBindings.register();
    ModScreenButtons.register();
    HandledScreens.register(ModScreenHandlers.TERMINAL, TerminalScreen::new);
    ClientTickEvents.END_CLIENT_TICK.register(TooManyChestsClient::pollQuickStack);
  }

  private static void pollQuickStack(MinecraftClient client) {
    final KeyBinding quickStack = ModKeyBindings.quickStack();
    if (quickStack == null) {
      return;
    }
    while (quickStack.wasPressed()) {
      QuickStackRequests.send();
    }
  }
}
