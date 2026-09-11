package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigListener;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
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
  public void serverConfigChanged(MinecraftServer server, TooManyChestsConfig config) {
    if (server == null || config == null) {
      return;
    }
    power.rescanAllIfRegionChanged(server);
  }
}
