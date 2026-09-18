package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

public final class TerminalCapacityBroadcast {
  public static final int INTERVAL_TICKS = 20;

  private final NetworkCapacityService capacities;
  private final NetworkCapacityWatch watch;

  private int lastWarningPercent = -1;

  public TerminalCapacityBroadcast() {
    this(new NetworkCapacityService(), new NetworkCapacityWatch());
  }

  public TerminalCapacityBroadcast(NetworkCapacityService capacities, NetworkCapacityWatch watch) {
    this.capacities = Objects.requireNonNull(capacities, "capacities");
    this.watch = Objects.requireNonNull(watch, "watch");
  }

  public void register() {
    ServerTickEvents.END_SERVER_TICK.register(this::tick);
  }

  public static boolean isDue(int ticks) {
    return Math.floorMod(ticks, INTERVAL_TICKS) == 0;
  }

  public void sweep(MinecraftServer server) {
    final List<TerminalAddress> running = TerminalNetworks.registry().addresses();
    watch.retainOnly(running);
    final int warningPercent = ServerConfigService.current().nearCapacityWarningPercent();
    final boolean thresholdMoved = warningPercent != lastWarningPercent;
    lastWarningPercent = warningPercent;
    for (final TerminalAddress terminal : running) {
      final NetworkMembership network = TerminalNetworks.registry().find(terminal).orElse(null);
      final ServerWorld world = server.getWorld(terminal.worldKey());
      if (network == null || world == null) {
        continue;
      }
      final NetworkCapacityStamp stamp =
          NetworkCapacityStamp.of(
              network.session(), TerminalLookup.settingsAt(world, terminal.pos()).orElse(null));
      if (!thresholdMoved && !watch.needsMeasuring(terminal, stamp)) {
        continue;
      }
      TerminalCapacitySignal.show(
          world,
          terminal.pos(),
          TerminalCapacitySignal.warns(
              world.getBlockState(terminal.pos()),
              capacities.measure(world, terminal.pos()),
              warningPercent));
      watch.measured(terminal, stamp);
    }
  }

  private void tick(MinecraftServer server) {
    if (server == null || !isDue(server.getTicks())) {
      return;
    }
    sweep(server);
  }
}
