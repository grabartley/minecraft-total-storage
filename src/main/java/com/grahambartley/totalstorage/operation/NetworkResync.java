package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.screenhandler.ContainerViewerSync;
import com.grahambartley.totalstorage.screenhandler.TerminalViewerSync;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import com.grahambartley.totalstorage.terminal.TerminalsHolding;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NetworkResync {
  private static final TerminalsHolding<NetworkMembership> HOLDING =
      new TerminalsHolding<>(TerminalNetworks.registry(), NetworkMembership::containers);

  private NetworkResync() {}

  public static int after(
      MinecraftServer server, Collection<StorageOrigin> changed, UUID actingPlayer) {
    if (server == null || changed == null || changed.isEmpty()) {
      return 0;
    }
    return terminals(server, changed, actingPlayer)
        + ContainerViewerSync.resyncOrigins(server, changed);
  }

  private static int terminals(
      MinecraftServer server, Collection<StorageOrigin> changed, UUID actingPlayer) {
    int resynced = 0;
    for (final TerminalAddress terminal : HOLDING.any(changed)) {
      for (final ServerPlayerEntity viewer : TerminalViewerSync.playersOn(server, terminal)) {
        if (viewer.getUuid().equals(actingPlayer)) {
          continue;
        }
        TerminalResync.of(viewer);
        resynced++;
      }
    }
    return resynced;
  }
}
