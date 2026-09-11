package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.ContainerEligibilityWatch;
import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigListener;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;

public final class ContainerEligibilityRefresh implements ServerConfigListener {
  private static final ContainerEligibilityWatch OVERRIDES = new ContainerEligibilityWatch();

  private final TerminalPower power;

  public ContainerEligibilityRefresh(TerminalPower power) {
    this.power = Objects.requireNonNull(power, "power");
  }

  public static ContainerEligibilityWatch overrides() {
    return OVERRIDES;
  }

  public static void register() {
    ServerConfigChanges.subscribe(new ContainerEligibilityRefresh(TerminalNetworks.power()));
  }

  @Override
  public void serverConfigChanged(MinecraftServer server, TooManyChestsConfig config) {
    if (server == null || config == null || !OVERRIDES.moved(config)) {
      return;
    }
    power.rescanAll(server);
  }
}
