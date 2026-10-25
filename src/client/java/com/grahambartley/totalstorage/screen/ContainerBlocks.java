package com.grahambartley.totalstorage.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public final class ContainerBlocks {
  private ContainerBlocks() {}

  public static ItemStack at(BlockPos container) {
    final MinecraftClient client = MinecraftClient.getInstance();
    if (client.world == null || !client.world.isChunkLoaded(container)) {
      return ItemStack.EMPTY;
    }
    return new ItemStack(client.world.getBlockState(container).getBlock().asItem());
  }
}
