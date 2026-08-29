package com.grahambartley.toomanychests.block;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.block.entity.TerminalBlockEntity;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalPower;
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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
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
 * <p>Power is the terminal's on switch, per ADR 0007. The block state carries it so the lit model
 * resolves and so a client can tell an inert terminal from a working one without being told; what
 * the signal actually starts and stops lives in {@link TerminalPower}.
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

  /**
   * The screen faces whoever placed the terminal, as a furnace's front does, and a terminal placed
   * into an already powered position starts powered rather than waiting for a neighbour update that
   * will never come.
   */
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

  /**
   * A player who has forgotten the power requirement gets told, because a terminal that does
   * nothing and says nothing is indistinguishable from a broken mod.
   */
  @Override
  protected ActionResult onUse(
      BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    if (world.isClient) {
      return ActionResult.SUCCESS;
    }
    if (!state.get(POWERED)) {
      player.sendMessage(Text.translatable("block.too-many-chests.terminal.unpowered"), true);
      return ActionResult.SUCCESS;
    }
    TooManyChests.LOGGER.debug(
        "{} opened the terminal at {}, which has no interface yet",
        player.getName().getString(),
        pos);
    return ActionResult.SUCCESS;
  }
}
