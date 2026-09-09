package com.grahambartley.toomanychests.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

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

  public boolean touchesChunk(ChunkPos chunkPos) {
    return chunkPos != null
        && chunkPos.x >= (minX() >> 4)
        && chunkPos.x <= (maxX() >> 4)
        && chunkPos.z >= (minZ() >> 4)
        && chunkPos.z <= (maxZ() >> 4);
  }

  public long blockVolume() {
    final long side = 2L * radius + 1L;
    return side * side * side;
  }
}
