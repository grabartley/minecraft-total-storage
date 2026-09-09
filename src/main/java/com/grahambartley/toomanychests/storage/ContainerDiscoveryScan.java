package com.grahambartley.toomanychests.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ContainerDiscoveryScan {
  private static final Logger LOGGER = LoggerFactory.getLogger(ContainerDiscoveryScan.class);

  private static final Comparator<BlockPos> POSITION_ORDER =
      Comparator.comparingInt(BlockPos::getX)
          .thenComparingInt(BlockPos::getY)
          .thenComparingInt(BlockPos::getZ);

  private final StorageProviderResolver resolver;
  private final NetworkEligibility eligibility;

  public ContainerDiscoveryScan() {
    this(new StorageProviderResolver(), new NetworkEligibility());
  }

  public ContainerDiscoveryScan(StorageProviderResolver resolver, NetworkEligibility eligibility) {
    this.resolver = Objects.requireNonNull(resolver, "resolver");
    this.eligibility = Objects.requireNonNull(eligibility, "eligibility");
  }

  public ContainerScanResult scan(World world, ScanRegion region) {
    Objects.requireNonNull(world, "world");
    Objects.requireNonNull(region, "region");
    final long startedAt = System.nanoTime();

    int chunksVisited = 0;
    int chunksSkipped = 0;
    final List<BlockPos> candidates = new ArrayList<>();
    for (final ChunkPos chunkPos : region.chunkPositions()) {
      final WorldChunk chunk = world.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z, false);
      if (chunk == null) {
        chunksSkipped++;
        continue;
      }
      chunksVisited++;
      for (final BlockPos pos : chunk.getBlockEntityPositions()) {
        if (region.contains(pos)) {
          candidates.add(pos.toImmutable());
        }
      }
    }
    candidates.sort(POSITION_ORDER);

    final ContainerScanResult result =
        new ContainerScanResult(
            collectProviders(world, candidates),
            chunksVisited,
            chunksSkipped,
            System.nanoTime() - startedAt);
    if (result.exceededBudget()) {
      LOGGER.warn(
          "Container scan around {} radius {} took {} ms over {} chunks and found {} containers",
          region.center(),
          region.radius(),
          result.elapsedNanos() / 1_000_000L,
          result.chunksVisited(),
          result.containerCount());
    }
    return result;
  }

  private List<StorageProvider> collectProviders(World world, List<BlockPos> candidates) {
    final List<StorageProvider> providers = new ArrayList<>();

    final Set<Object> claimedSlots = Collections.newSetFromMap(new IdentityHashMap<>());
    for (final BlockPos pos : candidates) {
      if (!eligibility.isEligible(world, pos)) {
        continue;
      }
      final StorageProvider provider = resolver.resolve(world, pos).orElse(null);
      if (provider == null) {
        continue;
      }
      final Object firstSlot = provider.slot(0).orElse(null);
      if (firstSlot != null && !claimedSlots.add(firstSlot)) {
        continue;
      }
      providers.add(provider);
    }
    return providers;
  }
}
