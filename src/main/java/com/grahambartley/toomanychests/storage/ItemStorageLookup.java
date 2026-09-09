package com.grahambartley.toomanychests.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ItemStorageLookup {
  @Nullable
  Storage<ItemVariant> find(World world, BlockPos pos);
}
