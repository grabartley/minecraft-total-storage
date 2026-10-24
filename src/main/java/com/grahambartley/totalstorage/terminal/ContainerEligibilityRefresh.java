package com.grahambartley.totalstorage.terminal;

import com.grahambartley.totalstorage.config.ContainerEligibilityWatch;
import com.grahambartley.totalstorage.config.ServerConfigChanges;
import com.grahambartley.totalstorage.config.ServerConfigListener;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;

public final class ContainerEligibilityRefresh implements ServerConfigListener {
  private static final ContainerEligibilityRefresh INSTANCE =
      new ContainerEligibilityRefresh(TerminalNetworks.power());

  private final TerminalPower power;
  private final ContainerEligibilityWatch overrides = new ContainerEligibilityWatch();

  private ContainerEligibilityRefresh(TerminalPower power) {
    this.power = Objects.requireNonNull(power, "power");
  }

  public static void register() {
    ServerConfigChanges.subscribe(INSTANCE);
  }

  public static void seedFrom(TotalStorageConfig config) {
    INSTANCE.overrides.seed(config);
  }

  @Override
  public void serverConfigChanged(MinecraftServer server, TotalStorageConfig config) {
    if (server == null || config == null || !overrides.moved(config)) {
      return;
    }
    power.rescanAll(server);
  }
}
