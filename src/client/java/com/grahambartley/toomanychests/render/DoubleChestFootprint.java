package com.grahambartley.toomanychests.render;

import java.util.Optional;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class DoubleChestFootprint {
  private DoubleChestFootprint() {}

  public static Optional<Direction> towardsPartner(Direction facing, ChestType type) {
    if (facing == null || type == null || type == ChestType.SINGLE) {
      return Optional.empty();
    }
    return Optional.of(
        type == ChestType.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());
  }

  public static Optional<BlockPos> partnerOf(BlockPos half, Direction facing, ChestType type) {
    if (half == null) {
      return Optional.empty();
    }
    return towardsPartner(facing, type).map(half::offset);
  }

  public static boolean joins(
      Direction facing, ChestType type, Direction partnerFacing, ChestType partnerType) {
    return type != null
        && type != ChestType.SINGLE
        && facing == partnerFacing
        && partnerType == type.getOpposite();
  }
}
