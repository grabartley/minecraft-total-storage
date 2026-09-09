package com.grahambartley.toomanychests.terminal;

import java.util.Objects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public record TerminalAddress(RegistryKey<World> worldKey, BlockPos pos) {
  public TerminalAddress {
    Objects.requireNonNull(worldKey, "worldKey");
    Objects.requireNonNull(pos, "pos");

    pos = pos.toImmutable();
  }

  public static TerminalAddress of(World world, BlockPos pos) {
    Objects.requireNonNull(world, "world");
    return new TerminalAddress(world.getRegistryKey(), pos);
  }

  public String describe() {
    return worldKey.getValue() + " [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
  }
}
