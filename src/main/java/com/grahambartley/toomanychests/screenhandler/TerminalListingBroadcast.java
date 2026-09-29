package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.operation.TerminalResync;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalListingBroadcast {
  private TerminalListingBroadcast() {}

  public static void register() {
    ServerTickEvents.END_SERVER_TICK.register(TerminalListingBroadcast::tick);
  }

  private static void tick(MinecraftServer server) {
    for (final TerminalAddress terminal : TerminalNetworks.registry().addresses()) {
      final NetworkMembership network = TerminalNetworks.registry().find(terminal).orElse(null);
      if (network == null || !network.session().isStale()) {
        continue;
      }
      for (final ServerPlayerEntity viewer : TerminalViewerSync.playersOn(server, terminal)) {
        TerminalResync.contents(viewer);
      }
    }
  }
}
