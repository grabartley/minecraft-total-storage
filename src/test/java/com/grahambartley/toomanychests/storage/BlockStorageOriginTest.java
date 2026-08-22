package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
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
