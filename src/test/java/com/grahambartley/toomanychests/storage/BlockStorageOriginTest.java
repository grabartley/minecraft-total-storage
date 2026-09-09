package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BlockStorageOriginTest {
  private static final RegistryKey<World> OVERWORLD =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "overworld"));
  private static final RegistryKey<World> NETHER =
      RegistryKey.of(RegistryKeys.WORLD, Identifier.of("minecraft", "the_nether"));

  @Test
  void sameWorldAndPositionAreEqualAndShareHashCode() {
    BlockStorageOrigin first = new BlockStorageOrigin(OVERWORLD, new BlockPos(4, 64, -9));
    BlockStorageOrigin second = new BlockStorageOrigin(OVERWORLD, new BlockPos(4, 64, -9));

    assertEquals(first, second);
    assertEquals(first.hashCode(), second.hashCode());
  }

  @ParameterizedTest
  @CsvSource({"5, 64, -9", "4, 65, -9", "4, 64, -8"})
  void differentPositionsAreNotEqual(int x, int y, int z) {
    BlockStorageOrigin origin = new BlockStorageOrigin(OVERWORLD, new BlockPos(4, 64, -9));

    assertNotEquals(origin, new BlockStorageOrigin(OVERWORLD, new BlockPos(x, y, z)));
  }

  @Test
  void samePositionInDifferentWorldsIsNotEqual() {
    BlockPos pos = new BlockPos(4, 64, -9);

    assertNotEquals(new BlockStorageOrigin(OVERWORLD, pos), new BlockStorageOrigin(NETHER, pos));
  }

  @Test
  void mutablePositionIsCopiedSoLaterMovementDoesNotChangeIdentity() {
    BlockPos.Mutable cursor = new BlockPos.Mutable(4, 64, -9);
    BlockStorageOrigin origin = new BlockStorageOrigin(OVERWORLD, cursor);

    cursor.set(100, 100, 100);

    assertEquals(new BlockStorageOrigin(OVERWORLD, new BlockPos(4, 64, -9)), origin);
  }

  @Test
  void describeNamesTheWorldAndCoordinates() {
    BlockStorageOrigin origin = new BlockStorageOrigin(OVERWORLD, new BlockPos(4, 64, -9));

    assertEquals("minecraft:overworld [4, 64, -9]", origin.describe());
  }

  @Test
  void ofTakesTheRegistryKeyFromTheWorld() {
    World world = mock(World.class);
    when(world.getRegistryKey()).thenReturn(NETHER);

    assertEquals(
        new BlockStorageOrigin(NETHER, new BlockPos(1, 2, 3)),
        BlockStorageOrigin.of(world, new BlockPos(1, 2, 3)));
  }

  @ParameterizedTest
  @CsvSource({
    "0, 0, 0, 0",
    "15, 15, 0, 0",
    "16, 0, 1, 0",
    "-1, -1, -1, -1",
    "-16, -16, -1, -1",
    "-17, 0, -2, 0",
  })
  void aContainerKnowsWhichChunkColumnHoldsIt(int x, int z, int chunkX, int chunkZ) {
    BlockStorageOrigin origin = new BlockStorageOrigin(OVERWORLD, new BlockPos(x, 64, z));

    assertTrue(origin.isIn(OVERWORLD, new ChunkPos(chunkX, chunkZ)));
  }

  @Test
  void aContainerIsNotInANeighbouringChunk() {
    BlockStorageOrigin origin = new BlockStorageOrigin(OVERWORLD, new BlockPos(8, 64, 8));

    assertFalse(origin.isIn(OVERWORLD, new ChunkPos(1, 0)));
    assertFalse(origin.isIn(OVERWORLD, new ChunkPos(0, 1)));
  }

  @Test
  void theSameChunkColumnInAnotherWorldIsNotThisContainersChunk() {
    BlockStorageOrigin origin = new BlockStorageOrigin(OVERWORLD, new BlockPos(8, 64, 8));

    assertFalse(origin.isIn(NETHER, new ChunkPos(0, 0)));
  }

  @Test
  void noChunkIsNotThisContainersChunk() {
    BlockStorageOrigin origin = new BlockStorageOrigin(OVERWORLD, new BlockPos(8, 64, 8));

    assertFalse(origin.isIn(OVERWORLD, null));
  }

  @Test
  void nullWorldKeyIsRejected() {
    BlockPos pos = new BlockPos(1, 2, 3);

    assertThrows(NullPointerException.class, () -> new BlockStorageOrigin(null, pos));
  }

  @Test
  void nullPositionIsRejected() {
    assertThrows(NullPointerException.class, () -> new BlockStorageOrigin(OVERWORLD, null));
  }

  @Test
  void nullWorldIsRejectedByFactory() {
    BlockPos pos = new BlockPos(1, 2, 3);

    assertThrows(NullPointerException.class, () -> BlockStorageOrigin.of(null, pos));
  }
}
