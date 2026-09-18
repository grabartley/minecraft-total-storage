package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import java.util.Objects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class TerminalCapacityBroadcast {
  public static final int INTERVAL_TICKS = 20;

  private final NetworkCapacityService capacities;

  public TerminalCapacityBroadcast() {
    this(new NetworkCapacityService());
  }

  public TerminalCapacityBroadcast(NetworkCapacityService capacities) {
    this.capacities = Objects.requireNonNull(capacities, "capacities");
  }

  public void register() {
    ServerTickEvents.END_SERVER_TICK.register(this::tick);
  }

  public static boolean isDue(int ticks) {
    return Math.floorMod(ticks, INTERVAL_TICKS) == 0;
  }

  private void tick(MinecraftServer server) {
    if (server == null || !isDue(server.getTicks())) {
      return;
    }
    final int warningPercent = ServerConfigService.current().nearCapacityWarningPercent();
    for (final TerminalAddress terminal : TerminalNetworks.registry().addresses()) {
      final NetworkMembership network = TerminalNetworks.registry().find(terminal).orElse(null);
      final ServerWorld world = server.getWorld(terminal.worldKey());
      if (network == null || world == null) {
        continue;
      }
      TerminalCapacitySignal.show(
          world,
          terminal.pos(),
          TerminalCapacitySignal.warns(
              world.getBlockState(terminal.pos()),
              capacities.measure(world, terminal.pos()),
              warningPercent));
    }
  }
}
