package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import java.util.Objects;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sends every open terminal a fresh page once the network under it has moved.
 *
 * <p>A player watching a listing while a hopper fills a chest behind them is looking at a number
 * quietly going stale. Waiting for them to scroll before correcting it would leave the terminal
 * right only by accident.
 *
 * <p>Driven by whether the index owes a recompute rather than by the mutations themselves. A hopper
 * chain moving a hundred items marks the network stale a hundred times and is answered with one
 * page, and reading the index to build that page is what clears the debt, so a network nobody
 * changed costs one boolean per tick.
 *
 * <p>Only terminals somebody is looking at are read. A network with no viewer has nothing to tell
 * anyone, and its index is left unread until somebody opens it.
 */
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
