package com.grahambartley.totalstorage.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class SidedItemStorageLookup implements ItemStorageLookup {
  public static final SidedItemStorageLookup INSTANCE = new SidedItemStorageLookup();

  private SidedItemStorageLookup() {}

  @Override
  public @Nullable Storage<ItemVariant> find(World world, BlockPos pos) {
    return ItemStorage.SIDED.find(world, pos, null);
  }
}
