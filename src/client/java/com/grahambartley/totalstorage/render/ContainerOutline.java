package com.grahambartley.totalstorage.render;

import java.util.Optional;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public final class ContainerOutline {
  private ContainerOutline() {}

  public static Box of(@Nullable BlockView world, BlockPos container) {
    final Box single = new Box(container);
    return partnerOf(world, container)
        .map(partner -> single.union(new Box(partner)))
        .orElse(single);
  }

  private static Optional<BlockPos> partnerOf(@Nullable BlockView world, BlockPos container) {
    if (world == null) {
      return Optional.empty();
    }
    final BlockState state = world.getBlockState(container);
    if (!(state.getBlock() instanceof ChestBlock)) {
      return Optional.empty();
    }
    final ChestType type = state.get(ChestBlock.CHEST_TYPE);
    if (!DoubleChestFootprint.hasAPartner(type)) {
      return Optional.empty();
    }
    final BlockPos partner = container.offset(ChestBlock.getFacing(state));
    return stillJoined(world, partner, state.getBlock(), state.get(ChestBlock.FACING), type)
        ? Optional.of(partner)
        : Optional.empty();
  }

  private static boolean stillJoined(
      BlockView world, BlockPos partner, Block chest, Direction facing, ChestType type) {
    final BlockState state = world.getBlockState(partner);
    if (state.getBlock() != chest) {
      return false;
    }
    return DoubleChestFootprint.joins(
        facing, type, state.get(ChestBlock.FACING), state.get(ChestBlock.CHEST_TYPE));
  }
}
