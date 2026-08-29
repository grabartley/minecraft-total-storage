package com.grahambartley.toomanychests.block;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.ServerConfigService;
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
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * The block that defines a storage network. It holds no network state itself: everything a network
 * remembers lives in its block entity, and the only thing the block state carries is what has to be
 * rendered, which is which way the screen points and whether it is lit.
 *
 * <p>The powered property is declared here but not yet driven. The shipped blockstate file covers
 * all eight facing and power combinations, so a terminal that did not expose both properties would
 * resolve to no model at all.
 */
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

  /** The screen faces whoever placed the terminal, as a furnace's front does. */
  @Override
  public BlockState getPlacementState(ItemPlacementContext ctx) {
    return this.getDefaultState()
        .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
        .with(POWERED, Boolean.FALSE);
  }

  @Override
  public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
    return new TerminalBlockEntity(pos, state);
  }

  /**
   * Ownership is recorded by UUID rather than by name so it survives a name change, and the access
   * mode is taken from the server's default at placement so a server that has opened its terminals
   * up does not have to be told again by every player. See ADR 0008.
   */
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
  protected ActionResult onUse(
      BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    if (world.isClient) {
      return ActionResult.SUCCESS;
    }
    TooManyChests.LOGGER.debug(
        "{} opened the terminal at {}, which has no interface yet",
        player.getName().getString(),
        pos);
    return ActionResult.SUCCESS;
  }
}
