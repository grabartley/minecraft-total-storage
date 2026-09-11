package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigListener;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.lifecycle.NextTick;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.minecraft.server.MinecraftServer;

public final class TerminalPanelRefresh implements ServerConfigListener {
  private static final Object PENDING = new Object();

  public static void register() {
    ServerConfigChanges.subscribe(new TerminalPanelRefresh());
  }

  @Override
  public void serverConfigChanged(MinecraftServer server, TooManyChestsConfig config) {
    if (server == null || config == null) {
      return;
    }
    NextTick.queue().submit(PENDING, () -> refreshEveryViewer(server));
  }

  private static void refreshEveryViewer(MinecraftServer server) {
    for (final TerminalAddress terminal : TerminalNetworks.registry().addresses()) {
      TerminalViewerSync.refresh(server, terminal);
    }
  }
}
