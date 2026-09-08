package com.grahambartley.toomanychests.screenhandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Tells everyone who has a container open by hand that its contents have moved.
 *
 * <p>Two players at one chest hold two separate screen handlers over one inventory, and a terminal
 * moving items through the network is a third writer neither of them can see. Whoever is looking at
 * the chest would otherwise keep clicking the old layout until something forced a refresh, and
 * every one of those clicks would be a slot that no longer holds what they think.
 *
 * <p>The whole screen is resent rather than a diff, because these operations move most of a
 * container at once and a resync that is nearly complete is the one that leaves an item on a client
 * that no longer exists on the server.
 *
 * <p>Half of a double chest counts as the whole of it. A change names a block entity, while a
 * player who opened the chest by hand is looking at one inventory spanning both halves, so matching
 * on the block entity alone would leave that screen stale exactly when it was written to. Every
 * player who opens one gets their own spanning inventory, which is why the changed side is always
 * named as block entities and never as a screen's own view of them.
 */
public final class ContainerViewerSync {
  private ContainerViewerSync() {}

  public static int resync(MinecraftServer server, Inventory inventory) {
    return inventory == null ? 0 : resyncAll(server, Set.of(inventory));
  }

  /** Returns how many screens were resent, which is what a caller logs or asserts on. */
  public static int resyncAll(MinecraftServer server, Collection<Inventory> inventories) {
    if (server == null || inventories == null || inventories.isEmpty()) {
      return 0;
    }
    final Set<Inventory> changed = new HashSet<>(inventories);
    int resynced = 0;
    for (final ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
      final ScreenHandler handler = viewer.currentScreenHandler;
      if (handler != null && shows(handler, changed)) {
        handler.syncState();
        resynced++;
      }
    }
    return resynced;
  }

  /**
   * Distinct inventories rather than slots, because a container screen repeats one inventory across
   * dozens of slots and the question is the same for every one of them.
   */
  private static boolean shows(ScreenHandler handler, Set<Inventory> changed) {
    final Set<Inventory> shown = new HashSet<>();
    for (final Slot slot : handler.slots) {
      if (changed.contains(slot.inventory)) {
        return true;
      }
      shown.add(slot.inventory);
    }
    return shown.stream().anyMatch(inventory -> isHalfOf(inventory, changed));
  }

  private static boolean isHalfOf(Inventory shown, Set<Inventory> changed) {
    if (!(shown instanceof DoubleInventory doubled)) {
      return false;
    }
    for (final Inventory inventory : changed) {
      if (doubled.isPart(inventory)) {
        return true;
      }
    }
    return false;
  }
}
