package com.grahambartley.toomanychests.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Queries {@link ItemStorage#SIDED} with a null direction, which yields the whole inventory rather
 * than one face's view, and pre-combines both halves of a double chest into one storage.
 */
public final class SidedItemStorageLookup implements ItemStorageLookup {
  public static final SidedItemStorageLookup INSTANCE = new SidedItemStorageLookup();

  private SidedItemStorageLookup() {}

  @Override
  public @Nullable Storage<ItemVariant> find(World world, BlockPos pos) {
    return ItemStorage.SIDED.find(world, pos, null);
  }
}
