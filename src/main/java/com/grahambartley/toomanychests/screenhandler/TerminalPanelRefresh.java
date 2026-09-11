package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigListener;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.network.ModNetworking;
import com.grahambartley.toomanychests.network.TerminalSettingsNetworking;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalPanelRefresh implements ServerConfigListener {
  public static void register() {
    ServerConfigChanges.subscribe(new TerminalPanelRefresh());
  }

  @Override
  public void serverConfigChanged(MinecraftServer server, TooManyChestsConfig config) {
    if (server == null || config == null) {
      return;
    }
    for (final TerminalAddress terminal : TerminalNetworks.registry().addresses()) {
      for (final ServerPlayerEntity viewer : TerminalViewerSync.playersOn(server, terminal)) {
        TerminalSettingsNetworking.panels().refresh(viewer);
        ModNetworking.listings().refresh(viewer);
      }
    }
  }
}
