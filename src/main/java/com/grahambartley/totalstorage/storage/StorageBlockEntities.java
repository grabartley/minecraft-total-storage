package com.grahambartley.totalstorage.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.block.Block;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;

public final class StorageBlockEntities {
  private StorageBlockEntities() {}

  public static boolean mayHoldItems(BlockEntity blockEntity) {
    if (blockEntity == null) {
      return false;
    }
    if (blockEntity instanceof Inventory) {
      return true;
    }
    final Block block = blockEntity.getCachedState().getBlock();
    return block instanceof InventoryProvider || ItemStorage.SIDED.getProvider(block) != null;
  }
}
