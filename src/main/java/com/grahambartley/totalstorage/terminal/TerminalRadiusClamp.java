package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.config.ServerConfigChanges;
import com.grahambartley.totalstorage.config.ServerConfigListener;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;

public final class TerminalRadiusClamp implements ServerConfigListener {
  private final TerminalPower power;

  public TerminalRadiusClamp(TerminalPower power) {
    this.power = Objects.requireNonNull(power, "power");
  }

  public static void register() {
    ServerConfigChanges.subscribe(new TerminalRadiusClamp(TerminalNetworks.power()));
  }

  @Override
  public void serverConfigChanged(MinecraftServer server, TotalStorageConfig config) {
    if (server == null || config == null) {
      return;
    }
    power.rescanAllIfRegionChanged(server);
  }
}
