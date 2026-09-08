package com.grahambartley.toomanychests;

import com.grahambartley.toomanychests.network.ModNetworkingClient;
import com.grahambartley.toomanychests.render.ContainerHighlightRenderer;
import com.grahambartley.toomanychests.screen.TerminalScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class TooManyChestsClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    ModNetworkingClient.register();
    ContainerHighlightRenderer.register();
    HandledScreens.register(ModScreenHandlers.TERMINAL, TerminalScreen::new);
  }
}
