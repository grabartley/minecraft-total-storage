package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.ContainerDiscoveryScan;
import com.grahambartley.toomanychests.storage.ContainerScanResult;
import com.grahambartley.toomanychests.storage.ScanRegion;
import com.grahambartley.toomanychests.storage.StorageProvider;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

public final class ContainerDiscoveryScanGameTest implements FabricGameTest {
  private static final String CONTAINER_PAD = "too-many-chests:container_pad";
  private static final String BATCH = "discovery-scan";
  private static final int TICK_LIMIT = 20;

  private static final BlockPos CENTRE = new BlockPos(3, 1, 3);

  private static final int RADIUS = 1;
  private static final BlockPos INSIDE_NEAR = new BlockPos(2, 1, 2);
  private static final BlockPos INSIDE_FAR = new BlockPos(4, 1, 4);
  private static final BlockPos OUTSIDE_NEAR = new BlockPos(0, 1, 0);
  private static final BlockPos OUTSIDE_FAR = new BlockPos(6, 1, 6);

  private final ContainerDiscoveryScan scan = new ContainerDiscoveryScan();

  private static int loadedChunksIn(ServerWorld world, ScanRegion region) {
    int loaded = 0;
    for (ChunkPos chunkPos : region.chunkPositions()) {
      if (world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
        loaded++;
      }
    }
    return loaded;
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void onlyContainersInsideTheRadiusAreDiscovered(TestContext context) {
    for (BlockPos pos : List.of(INSIDE_NEAR, INSIDE_FAR, OUTSIDE_NEAR, OUTSIDE_FAR)) {
      context.setBlockState(pos, Blocks.BARREL.getDefaultState());
    }

    List<BlockPos> found = discoveredPositions(context);

    context.assertTrue(
        found.equals(absolute(context, List.of(INSIDE_NEAR, INSIDE_FAR))),
        "Expected only the in-range barrels, found " + found);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void ineligibleContainersInsideTheRadiusAreExcluded(TestContext context) {
    context.setBlockState(INSIDE_NEAR, Blocks.BARREL.getDefaultState());
    context.setBlockState(INSIDE_FAR, Blocks.FURNACE.getDefaultState());

    List<BlockPos> found = discoveredPositions(context);

    context.assertTrue(
        found.equals(absolute(context, List.of(INSIDE_NEAR))),
        "A furnace in range should be excluded, found " + found);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aDoubleChestIsDiscoveredOnceRatherThanPerHalf(TestContext context) {
    BlockPos leftHalf = new BlockPos(2, 1, 2);
    BlockPos rightHalf = new BlockPos(3, 1, 2);
    context.setBlockState(leftHalf, chestHalf(ChestType.LEFT));
    context.setBlockState(rightHalf, chestHalf(ChestType.RIGHT));

    ContainerScanResult result = scanHere(context);

    context.assertTrue(
        result.containerCount() == 1,
        "A double chest should be one container, found " + result.containerCount());
    context.assertTrue(
        result.providers().get(0).slotCount().orElseThrow() == 54,
        "The one provider should span both halves");
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void nothingInRangeYieldsAnEmptyResult(TestContext context) {
    context.setBlockState(OUTSIDE_FAR, Blocks.BARREL.getDefaultState());

    ContainerScanResult result = scanHere(context);

    context.assertTrue(
        result.containerCount() == 0,
        "Expected nothing in range, found " + result.containerCount());
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void repeatedScansReturnTheSameOrder(TestContext context) {
    for (BlockPos pos : List.of(INSIDE_FAR, INSIDE_NEAR)) {
      context.setBlockState(pos, Blocks.BARREL.getDefaultState());
    }

    List<BlockPos> first = discoveredPositions(context);
    List<BlockPos> second = discoveredPositions(context);

    context.assertTrue(first.equals(second), "Scans should be repeatable, got " + first + second);
    context.assertTrue(
        first.equals(absolute(context, List.of(INSIDE_NEAR, INSIDE_FAR))),
        "Results should be ordered by position, got " + first);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aScanReachingBeyondLoadedChunksLoadsNoneOfThem(TestContext context) {
    ServerWorld world = context.getWorld();
    ScanRegion wide = new ScanRegion(context.getAbsolutePos(CENTRE), 512);

    int loadedBefore = loadedChunksIn(world, wide);
    ContainerScanResult result = scan.scan(world, wide);
    int loadedAfter = loadedChunksIn(world, wide);

    context.assertTrue(
        result.skippedUnloadedChunks(),
        "A 512 block radius should reach unloaded chunks, skipped " + result.chunksSkipped());
    context.assertTrue(
        loadedAfter == loadedBefore,
        "Scanning must not load chunks in the region it scanned, went from "
            + loadedBefore
            + " to "
            + loadedAfter);
    context.complete();
  }

  @GameTest(templateName = CONTAINER_PAD, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void aWideScanWalksChunksRatherThanPositions(TestContext context) {
    ScanRegion wide = new ScanRegion(context.getAbsolutePos(CENTRE), 512);

    ContainerScanResult result = scan.scan(context.getWorld(), wide);

    context.assertTrue(
        wide.blockVolume() > 1_000_000_000L,
        "The region should hold more positions than any scan could visit");
    context.assertTrue(
        result.chunksVisited() + result.chunksSkipped() == wide.chunkPositions().size(),
        "Every chunk column should be accounted for as visited or skipped");
    context.complete();
  }

  private ContainerScanResult scanHere(TestContext context) {
    return scan.scan(context.getWorld(), new ScanRegion(context.getAbsolutePos(CENTRE), RADIUS));
  }

  private List<BlockPos> discoveredPositions(TestContext context) {
    return scanHere(context).providers().stream()
        .map(ContainerDiscoveryScanGameTest::positionOf)
        .toList();
  }

  private static BlockPos positionOf(StorageProvider provider) {
    return ((BlockStorageOrigin) provider.origin()).pos();
  }

  private static List<BlockPos> absolute(TestContext context, List<BlockPos> relative) {
    return relative.stream().map(context::getAbsolutePos).toList();
  }

  private static BlockState chestHalf(ChestType type) {
    return Blocks.CHEST
        .getDefaultState()
        .with(ChestBlock.FACING, Direction.NORTH)
        .with(ChestBlock.CHEST_TYPE, type);
  }
}
