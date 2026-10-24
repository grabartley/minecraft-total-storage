package com.grahambartley.totalstorage.render;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DoubleChestFootprintTest {
  @ParameterizedTest
  @EnumSource(
      value = ChestType.class,
      names = {"LEFT", "RIGHT"})
  void eitherHalfOfADoubleChestHasAPartner(ChestType type) {
    assertTrue(DoubleChestFootprint.hasAPartner(type));
  }

  @Test
  void aSingleChestHasNoPartner() {
    assertFalse(DoubleChestFootprint.hasAPartner(ChestType.SINGLE));
    assertFalse(DoubleChestFootprint.hasAPartner(null));
  }

  @Test
  void theOtherHalfHasToFaceTheSameWayAndBeTheOppositeSide() {
    assertTrue(
        DoubleChestFootprint.joins(
            Direction.NORTH, ChestType.LEFT, Direction.NORTH, ChestType.RIGHT));
    assertTrue(
        DoubleChestFootprint.joins(
            Direction.EAST, ChestType.RIGHT, Direction.EAST, ChestType.LEFT));
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
    assertEquals(
        false,
        DoubleChestFootprint.joins(
            Direction.NORTH, ChestType.LEFT, Direction.NORTH, ChestType.SINGLE));
  }
}
