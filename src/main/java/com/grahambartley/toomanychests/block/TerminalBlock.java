package com.grahambartley.toomanychests.block;

import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.screenhandler.TerminalOpener;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class TerminalBlock extends HorizontalFacingBlock implements BlockEntityProvider {
  public static final MapCodec<TerminalBlock> CODEC = createCodec(TerminalBlock::new);
  public static final BooleanProperty POWERED = Properties.POWERED;

  public TerminalBlock(Settings settings) {
    super(settings);
    this.setDefaultState(
        this.stateManager
            .getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(POWERED, Boolean.FALSE));
  }

  @Override
  protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
    return CODEC;
  }

  @Override
  protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
    builder.add(FACING, POWERED);
  }

  @Override
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return this.getDefaultState()
        .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
        .with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new TerminalBlockEntity(pos, state);
  }

  @Override
  public void onPlaced(
      World world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack itemStack) {
    super.onPlaced(world, pos, state, placer, itemStack);
    if (world.isClient
        || !(placer instanceof PlayerEntity player)
        || !(world.getBlockEntity(pos) instanceof TerminalBlockEntity terminal)) {
      return;
    }
    terminal.updateSettings(
        settings ->
            settings
                .withOwner(player.getUuid())
                .withAccessMode(ServerConfigService.current().defaultTerminalAccessMode()));
  }

  @Override
  protected void neighborUpdate(
      BlockState state,
      World world,
      BlockPos pos,
      Block sourceBlock,
      BlockPos sourcePos,
      boolean notify) {
    super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
    if (world instanceof ServerWorld serverWorld) {
      TerminalNetworks.power().signalChanged(serverWorld, pos, state);
    }
  }

  @Override
  protected ActionResult onUse(
      BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    if (world.isClient || !(world instanceof ServerWorld serverWorld)) {
      return ActionResult.SUCCESS;
    }
    if (player instanceof ServerPlayerEntity serverPlayer) {
      TerminalOpener.open(serverWorld, pos, serverPlayer);
    }
    return ActionResult.SUCCESS;
  }
}
