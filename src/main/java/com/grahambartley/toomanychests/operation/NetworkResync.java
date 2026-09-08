package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.screenhandler.ContainerViewerSync;
import com.grahambartley.toomanychests.screenhandler.TerminalViewerSync;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalsHolding;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

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
   * @return how many screens were resent
   */
  public static int after(
      MinecraftServer server, Collection<StorageOrigin> changed, UUID actingPlayer) {
    if (server == null || changed == null || changed.isEmpty()) {
      return 0;
    }
    return terminals(server, changed, actingPlayer)
        + ContainerViewerSync.resyncAll(server, inventoriesOf(server, changed));
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

  /**
   * The containers still standing, which is all a screen can be open on. One that has just been
   * broken has nothing to resync and its viewers were closed out with it.
   */
  private static List<Inventory> inventoriesOf(
      MinecraftServer server, Collection<StorageOrigin> changed) {
    final List<Inventory> inventories = new ArrayList<>(changed.size());
    for (final StorageOrigin origin : changed) {
      if (!(origin instanceof BlockStorageOrigin block)) {
        continue;
      }
      final ServerWorld world = server.getWorld(block.worldKey());
      if (world != null && world.getBlockEntity(block.pos()) instanceof Inventory inventory) {
        inventories.add(inventory);
      }
    }
    return inventories;
  }
}
