package com.grahambartley.toomanychests.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class DoubleChestFootprintTest {
  private static final BlockPos HALF = new BlockPos(4, 64, -7);

  static Stream<Arguments> everyHalfOfADoubleChest() {
    return Stream.of(
        Arguments.of(Direction.NORTH, ChestType.LEFT, Direction.EAST),
        Arguments.of(Direction.NORTH, ChestType.RIGHT, Direction.WEST),
        Arguments.of(Direction.SOUTH, ChestType.LEFT, Direction.WEST),
        Arguments.of(Direction.SOUTH, ChestType.RIGHT, Direction.EAST),
        Arguments.of(Direction.EAST, ChestType.LEFT, Direction.SOUTH),
        Arguments.of(Direction.EAST, ChestType.RIGHT, Direction.NORTH),
        Arguments.of(Direction.WEST, ChestType.LEFT, Direction.NORTH),
        Arguments.of(Direction.WEST, ChestType.RIGHT, Direction.SOUTH));
  }

  @ParameterizedTest
  @MethodSource("everyHalfOfADoubleChest")
  void aHalfKnowsWhichWayItsPartnerLies(
      Direction facing, ChestType type, Direction towardsPartner) {
    assertEquals(
        Optional.of(HALF.offset(towardsPartner)),
        DoubleChestFootprint.partnerOf(HALF, facing, type));
  }

  @ParameterizedTest
  @EnumSource(Direction.class)
  void aSingleChestHasNoPartner(Direction facing) {
    assertEquals(Optional.empty(), DoubleChestFootprint.partnerOf(HALF, facing, ChestType.SINGLE));
  }

  @Test
  void aChestWithoutAStateHasNoPartner() {
    assertEquals(Optional.empty(), DoubleChestFootprint.partnerOf(HALF, null, ChestType.LEFT));
    assertEquals(Optional.empty(), DoubleChestFootprint.partnerOf(HALF, Direction.NORTH, null));
    assertEquals(Optional.empty(), DoubleChestFootprint.partnerOf(null, Direction.NORTH, null));
  }

  @Test
  void theOtherHalfHasToFaceTheSameWayAndBeTheOppositeSide() {
    assertTrue(
        DoubleChestFootprint.joins(
            Direction.NORTH, ChestType.LEFT, Direction.NORTH, ChestType.RIGHT));
  }

  @Test
  void aNeighbourFacingAnotherWayIsItsOwnChest() {
    assertFalse(
        DoubleChestFootprint.joins(
            Direction.NORTH, ChestType.LEFT, Direction.SOUTH, ChestType.RIGHT));
  }

  @Test
  void aNeighbourOnTheSameSideIsItsOwnChest() {
    assertFalse(
        DoubleChestFootprint.joins(
            Direction.NORTH, ChestType.LEFT, Direction.NORTH, ChestType.LEFT));
  }

  @Test
  void aChestThatHasBeenSplitJoinsNothing() {
    assertFalse(
        DoubleChestFootprint.joins(
            Direction.NORTH, ChestType.SINGLE, Direction.NORTH, ChestType.SINGLE));
  }
}
