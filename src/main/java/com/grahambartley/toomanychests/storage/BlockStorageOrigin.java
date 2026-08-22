package com.grahambartley.toomanychests.storage;

import java.util.Objects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record BlockStorageOrigin(RegistryKey<World> worldKey, BlockPos pos)
    implements StorageOrigin {
  public BlockStorageOrigin {
    Objects.requireNonNull(worldKey, "worldKey");
    Objects.requireNonNull(pos, "pos");
    // Discovery scans reuse a BlockPos.Mutable, which would corrupt any map keyed by this origin.
    pos = pos.toImmutable();
  }

  public static BlockStorageOrigin of(World world, BlockPos pos) {
    Objects.requireNonNull(world, "world");
    return new BlockStorageOrigin(world.getRegistryKey(), pos);
  }

  @Override
  public String describe() {
    return worldKey.getValue() + " [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
  }
}
