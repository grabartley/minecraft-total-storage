package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.screenhandler.ContainerViewerSync;
import com.grahambartley.toomanychests.screenhandler.TerminalViewerSync;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalsHolding;
import java.util.Collection;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Puts everyone who can see a set of containers back in step with what they now hold.
 *
 * <p>Driven by what a committed operation really moved rather than by a sweep of the tick, because
 * the moment a resync matters most is the one between the commit and the next click. A player who
 * is not the one who acted has no way of knowing anything happened, and the standards this mod is
 * built to say the acting player is never the only one who needs telling.
 *
 * <p>Two audiences, reached separately because they are looking at different things. A terminal
 * viewer is looking at a listing computed from the index and gets a fresh page; a player with the
 * chest itself open is looking at slots and gets a container sync. A player who is both gets both,
 * once each.
 *
 * <p>Nobody outside those two sets hears anything. Terminals are narrowed to the ones whose network
 * actually holds a changed container, and container screens to the ones actually showing it, so a
 * withdrawal from one chest costs nothing to a player standing at an unrelated terminal.
 */
public final class NetworkResync {
  private static final TerminalsHolding<NetworkMembership> HOLDING =
      new TerminalsHolding<>(TerminalNetworks.registry(), NetworkMembership::containers);

  private NetworkResync() {}

  /**
   * Resyncs every viewer of these containers except the player who caused the change, whose own
   * screen the operation answers directly.
   *
   * <p>Only the terminal half excludes the acting player. A player has one open screen, so somebody
   * holding the container itself open is not also at a terminal, and the container half is exactly
   * where the acting player might be the one who needs telling.
   *
   * @return how many screens were resent, which is the surface a test asserts on rather than
   *     something a caller acts upon
   */
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
