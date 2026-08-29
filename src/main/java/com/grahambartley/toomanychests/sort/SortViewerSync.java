package com.grahambartley.toomanychests.sort;

import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Tells everyone looking at a container that it has been rearranged.
 *
 * <p>Two players at one chest hold two separate screen handlers over one inventory, so the sorting
 * player's own resync reaches nobody else. Whoever did not press the button would otherwise keep
 * clicking the old layout until something forced a refresh, and every one of those clicks would be
 * a slot that no longer holds what they think.
 *
 * <p>The whole screen is resent rather than a diff, because a sort moves most of a container at
 * once and a resync that is nearly complete is the one that leaves an item on a client that no
 * longer exists on the server.
 */
public final class SortViewerSync {
  private SortViewerSync() {}

  public static int resync(MinecraftServer server, Inventory inventory) {
    if (server == null || inventory == null) {
      return 0;
    }
    int resynced = 0;
    for (final ServerPlayerEntity viewer : server.getPlayerManager().getPlayerList()) {
      final ScreenHandler handler = viewer.currentScreenHandler;
      if (handler != null && shows(handler, inventory)) {
        handler.syncState();
        resynced++;
      }
    }
    return resynced;
  }

  private static boolean shows(ScreenHandler handler, Inventory inventory) {
    for (final Slot slot : handler.slots) {
      if (slot.inventory == inventory) {
        return true;
      }
    }
    return false;
  }
}
