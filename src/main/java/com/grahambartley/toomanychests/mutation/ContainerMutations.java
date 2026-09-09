package com.grahambartley.toomanychests.mutation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class ContainerMutations {
  private static final ContainerMutationRegistry REGISTRY = new ContainerMutationRegistry();

  private ContainerMutations() {}

  public static ContainerMutationRegistry registry() {
    return REGISTRY;
  }

  public static void publish(World world, BlockPos pos) {
    if (world == null || world.isClient()) {
      return;
    }
    REGISTRY.publish(world.getRegistryKey(), pos);
  }
}
