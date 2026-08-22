package com.grahambartley.toomanychests.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

/**
 * The volume a terminal reaches: a cuboid centred on the terminal whose radius is its half-extent.
 * A cuboid rather than a sphere because discovery walks chunks, and a cuboid maps onto whole chunk
 * columns without a per-position distance test.
 */
public record ScanRegion(BlockPos center, int radius) {
  public ScanRegion {
    Objects.requireNonNull(center, "center");
    center = center.toImmutable();
    radius = Math.max(0, radius);
  }

  public boolean contains(BlockPos pos) {
    return pos != null
        && Math.abs(pos.getX() - center.getX()) <= radius
        && Math.abs(pos.getY() - center.getY()) <= radius
        && Math.abs(pos.getZ() - center.getZ()) <= radius;
  }

  public int minX() {
    return center.getX() - radius;
  }

  public int maxX() {
    return center.getX() + radius;
  }

  public int minY() {
    return center.getY() - radius;
  }

  public int maxY() {
    return center.getY() + radius;
  }

  public int minZ() {
    return center.getZ() - radius;
  }

  public int maxZ() {
    return center.getZ() + radius;
  }

  /** Every chunk column the cuboid touches, in a fixed order so scans are repeatable. */
  public List<ChunkPos> chunkPositions() {
    final int minChunkX = minX() >> 4;
    final int maxChunkX = maxX() >> 4;
    final int minChunkZ = minZ() >> 4;
    final int maxChunkZ = maxZ() >> 4;
    final List<ChunkPos> positions =
        new ArrayList<>((maxChunkX - minChunkX + 1) * (maxChunkZ - minChunkZ + 1));
    for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
      for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
        positions.add(new ChunkPos(chunkX, chunkZ));
      }
    }
    return List.copyOf(positions);
  }

  /** How many block positions the cuboid holds, which is what walking chunks avoids testing. */
  public long blockVolume() {
    final long side = 2L * radius + 1L;
    return side * side * side;
  }
}
