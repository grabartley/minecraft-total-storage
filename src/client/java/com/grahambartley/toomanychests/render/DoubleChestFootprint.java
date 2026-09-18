package com.grahambartley.toomanychests.render;

import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.Direction;

public final class DoubleChestFootprint {
  private DoubleChestFootprint() {}

  public static boolean hasAPartner(ChestType type) {
    return type != null && type != ChestType.SINGLE;
  }

  public static boolean joins(
      Direction facing, ChestType type, Direction partnerFacing, ChestType partnerType) {
    return hasAPartner(type) && facing == partnerFacing && partnerType == type.getOpposite();
  }
}
