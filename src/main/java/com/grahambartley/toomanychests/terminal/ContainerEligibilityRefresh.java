package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.ContainerEligibilityWatch;
import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigListener;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class ContainerEligibilityRefresh implements ServerConfigListener {
  private final TerminalPower power;
  private final ContainerEligibilityWatch overrides = new ContainerEligibilityWatch();

  public ContainerEligibilityRefresh(TerminalPower power) {
    this.power = Objects.requireNonNull(power, "power");
  }

  public static void register() {
    ServerConfigChanges.subscribe(new ContainerEligibilityRefresh(TerminalNetworks.power()));
  }

  @Override
  public void serverConfigChanged(MinecraftServer server, TooManyChestsConfig config) {
    if (server == null || config == null || !overrides.moved(config)) {
      return;
    }
    for (final TerminalAddress address : power.registry().addresses()) {
      final ServerWorld world = server.getWorld(address.worldKey());
      if (world != null) {
        power.rescan(world, address.pos());
      }
    }
  }
}
