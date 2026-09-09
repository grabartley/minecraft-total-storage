package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.Objects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class TerminalListingBroadcast {
  private final TerminalListingService listings;

  public TerminalListingBroadcast(TerminalListingService listings) {
    this.listings = Objects.requireNonNull(listings, "listings");
  }

  public void register() {
    ServerTickEvents.END_SERVER_TICK.register(this::tick);
  }

  private void tick(MinecraftServer server) {
    for (final TerminalAddress terminal : TerminalNetworks.registry().addresses()) {
      final NetworkMembership network = TerminalNetworks.registry().find(terminal).orElse(null);
      if (network == null || !network.session().isStale()) {
        continue;
      }
      for (final ServerPlayerEntity viewer : TerminalViewerSync.playersOn(server, terminal)) {
        listings.refresh(viewer);
      }
    }
  }
}
