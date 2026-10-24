package com.grahambartley.totalstorage.lifecycle;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class LoadedChunks {
  private LoadedChunks() {}

  public static boolean holds(ServerWorld world, BlockPos pos) {
    if (world == null || pos == null) {
      return false;
    }
    return world.getChunkManager().isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4);
  }
}
