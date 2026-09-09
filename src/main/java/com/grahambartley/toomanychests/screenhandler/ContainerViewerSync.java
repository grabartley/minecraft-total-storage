package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public final class ContainerViewerSync {
  private ContainerViewerSync() {}

  public static int resync(MinecraftServer server, Inventory inventory) {
    return inventory == null ? 0 : resyncAll(server, Set.of(inventory));
  }

  public static int resyncOrigins(MinecraftServer server, Collection<StorageOrigin> origins) {
    return resyncAll(server, inventoriesOf(server, origins));
  }

  public static List<Inventory> inventoriesOf(
      MinecraftServer server, Collection<StorageOrigin> origins) {
    if (server == null || origins == null) {
      return List.of();
    }
    final List<Inventory> inventories = new ArrayList<>(origins.size());
    for (final StorageOrigin origin : origins) {
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
