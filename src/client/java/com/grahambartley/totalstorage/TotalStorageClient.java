package com.grahambartley.totalstorage;

import com.grahambartley.totalstorage.config.ClientPreferencesStore;
import com.grahambartley.totalstorage.network.ModNetworkingClient;
import com.grahambartley.totalstorage.render.ContainerHighlightRenderer;
import com.grahambartley.totalstorage.screen.HintTooltip;
import com.grahambartley.totalstorage.screen.ModKeyBindings;
import com.grahambartley.totalstorage.screen.ModScreenButtons;
import com.grahambartley.totalstorage.screen.QuickStackRequests;
import com.grahambartley.totalstorage.screen.SortRequests;
import com.grahambartley.totalstorage.screen.TerminalScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.option.KeyBinding;

public class TotalStorageClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ClientPreferencesStore.load();
    ModNetworkingClient.register();
    ContainerHighlightRenderer.register();
    ModKeyBindings.register();
    ModScreenButtons.register();
    HintTooltip.register();
    HandledScreens.register(ModScreenHandlers.TERMINAL, TerminalScreen::new);
    ClientTickEvents.END_CLIENT_TICK.register(TotalStorageClient::pollQuickStack);
    ClientTickEvents.END_CLIENT_TICK.register(TotalStorageClient::pollSort);
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

  private static void pollSort(MinecraftClient client) {
    final KeyBinding sort = ModKeyBindings.sort();
    if (sort == null) {
      return;
    }
    boolean pressed = false;
    while (sort.wasPressed()) {
      pressed = true;
    }
    if (pressed && client.currentScreen == null && client.player != null) {
      SortRequests.send(client.player.playerScreenHandler.syncId);
    }
  }
}
