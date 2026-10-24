package com.grahambartley.totalstorage.screenhandler;

import com.grahambartley.totalstorage.config.ServerConfigChanges;
import com.grahambartley.totalstorage.config.ServerConfigListener;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.lifecycle.NextTick;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import net.minecraft.server.MinecraftServer;

public final class TerminalPanelRefresh implements ServerConfigListener {
  private static final Object PENDING = new Object();

  public static void register() {
    ServerConfigChanges.subscribe(new TerminalPanelRefresh());
  }

  @Override
  public void serverConfigChanged(MinecraftServer server, TotalStorageConfig config) {
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
