package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ScanRegionTest {
  private static final BlockPos CENTER = new BlockPos(100, 64, -200);

  @ParameterizedTest
  @CsvSource({
    "100, 64, -200", // the centre itself
    "104, 64, -200", // on the x face
    "96, 64, -200",
    "100, 68, -200", // on the y face
    "100, 60, -200",
    "100, 64, -196", // on the z face
    "100, 64, -204",
    "104, 68, -196", // the far corner is still inside a cuboid
  })
  void positionsWithinTheHalfExtentAreInside(int x, int y, int z) {
    assertTrue(new ScanRegion(CENTER, 4).contains(new BlockPos(x, y, z)));
  }

  @ParameterizedTest
  @CsvSource({
    "105, 64, -200",
    "95, 64, -200",
    "100, 69, -200",
    "100, 59, -200",
    "100, 64, -195",
    "100, 64, -205"
  })
  void positionsBeyondTheHalfExtentAreOutside(int x, int y, int z) {
    assertFalse(new ScanRegion(CENTER, 4).contains(new BlockPos(x, y, z)));
  }

  @Test
  void aCornerBeyondTheHalfExtentOnOneAxisAloneIsOutside() {
    assertFalse(new ScanRegion(CENTER, 4).contains(new BlockPos(104, 69, -196)));
  }

  @Test
  void aRegionWithNoRadiusHoldsOnlyItsCentre() {
    ScanRegion region = new ScanRegion(CENTER, 0);

    assertTrue(region.contains(CENTER));
    assertFalse(region.contains(CENTER.up()));
    assertEquals(1L, region.blockVolume());
  }

  @Test
  void aMissingPositionIsNotInside() {
    assertFalse(new ScanRegion(CENTER, 4).contains(null));
  }

  @ParameterizedTest
  @ValueSource(ints = {-1, -64})
  void aNegativeRadiusCollapsesToASinglePosition(int radius) {
    assertEquals(0, new ScanRegion(CENTER, radius).radius());
    assertEquals(1L, new ScanRegion(CENTER, radius).blockVolume());
  }

  @Test
  void theBoundsSpanTheHalfExtentOnEveryAxis() {
    ScanRegion region = new ScanRegion(CENTER, 4);

    assertEquals(96, region.minX());
    assertEquals(104, region.maxX());
    assertEquals(60, region.minY());
    assertEquals(68, region.maxY());
    assertEquals(-204, region.minZ());
    assertEquals(-196, region.maxZ());
  }

  @Test
  void aRegionInsideOneChunkTouchesOnlyThatChunk() {
    ScanRegion region = new ScanRegion(new BlockPos(8, 64, 8), 2);

    assertEquals(List.of(new ChunkPos(0, 0)), region.chunkPositions());
  }

  @Test
  void aRegionStraddlingAChunkBorderTouchesBothChunks() {
    ScanRegion region = new ScanRegion(new BlockPos(15, 64, 8), 1);

    assertEquals(List.of(new ChunkPos(0, 0), new ChunkPos(1, 0)), region.chunkPositions());
  }

  @Test
  void chunkColumnsAreListedInAFixedOrder() {
    ScanRegion region = new ScanRegion(new BlockPos(8, 64, 8), 20);

    assertEquals(region.chunkPositions(), region.chunkPositions());
    assertEquals(
        List.of(
            new ChunkPos(-1, -1),
            new ChunkPos(-1, 0),
            new ChunkPos(-1, 1),
            new ChunkPos(0, -1),
            new ChunkPos(0, 0),
            new ChunkPos(0, 1),
            new ChunkPos(1, -1),
            new ChunkPos(1, 0),
            new ChunkPos(1, 1)),
        region.chunkPositions());
  }

  @Test
  void chunkColumnsWorkAcrossNegativeCoordinates() {
    ScanRegion region = new ScanRegion(new BlockPos(-1, 64, -1), 0);

    assertEquals(List.of(new ChunkPos(-1, -1)), region.chunkPositions());
  }

  @Test
  void aSixteenBlockRadiusWalksFarFewerChunksThanItHoldsPositions() {
    ScanRegion region = new ScanRegion(CENTER, 16);

    assertEquals(35937L, region.blockVolume());
    assertTrue(
        region.chunkPositions().size() < 20,
        "Chunk columns touched should be trivial next to the block volume, was "
            + region.chunkPositions().size());
  }

  @Test
  void aMutableCentreIsCopiedSoLaterMovementDoesNotMoveTheRegion() {
    BlockPos.Mutable cursor = new BlockPos.Mutable(100, 64, -200);
    ScanRegion region = new ScanRegion(cursor, 1);

    cursor.set(0, 0, 0);

    assertEquals(CENTER, region.center());
    assertTrue(region.contains(CENTER));
  }

  @ParameterizedTest
  @CsvSource({
    "6, -13", // the centre's own chunk
    "5, -13", // the chunks the radius spills into
    "7, -13", "6, -12", "6, -14",
  })
  void aRegionReachesIntoEveryChunkItsRadiusSpillsInto(int chunkX, int chunkZ) {
    ScanRegion region = new ScanRegion(CENTER, 16);

    assertTrue(region.touchesChunk(new ChunkPos(chunkX, chunkZ)));
  }

  @ParameterizedTest
  @CsvSource({
    "4, -13", "8, -13", "6, -11", "6, -15", "0, 0",
  })
  void aRegionReachesNoFurtherThanItsRadius(int chunkX, int chunkZ) {
    ScanRegion region = new ScanRegion(CENTER, 16);

    assertFalse(region.touchesChunk(new ChunkPos(chunkX, chunkZ)));
  }

  @Test
  void everyChunkTheScanWalksIsAChunkTheRegionSaysItTouches() {
    ScanRegion region = new ScanRegion(CENTER, 24);

    assertTrue(region.chunkPositions().stream().allMatch(region::touchesChunk));
  }

  @Test
  void noChunkIsNotTouched() {
    assertFalse(new ScanRegion(CENTER, 4).touchesChunk(null));
  }

  @Test
  void aMissingCentreIsRejected() {
    assertThrows(NullPointerException.class, () -> new ScanRegion(null, 4));
  }
}
