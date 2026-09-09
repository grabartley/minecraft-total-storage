package com.grahambartley.toomanychests.storage;

import java.util.Objects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public record BlockStorageOrigin(RegistryKey<World> worldKey, BlockPos pos)
    implements StorageOrigin {
  public BlockStorageOrigin {
    Objects.requireNonNull(worldKey, "worldKey");
    Objects.requireNonNull(pos, "pos");

    pos = pos.toImmutable();
  }

  public static BlockStorageOrigin of(World world, BlockPos pos) {
    Objects.requireNonNull(world, "world");
    return new BlockStorageOrigin(world.getRegistryKey(), pos);
  }

  public boolean isIn(RegistryKey<World> otherWorldKey, ChunkPos chunkPos) {
    return worldKey.equals(otherWorldKey) && new ChunkPos(pos).equals(chunkPos);
  }

  @Override
  public String describe() {
    return worldKey.getValue() + " [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
  }
}
