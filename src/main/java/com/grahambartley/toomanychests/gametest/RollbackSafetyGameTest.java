package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.operation.DepositOutcome;
import com.grahambartley.toomanychests.operation.DepositResult;
import com.grahambartley.toomanychests.operation.DepositService;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.operation.WithdrawOutcome;
import com.grahambartley.toomanychests.operation.WithdrawResult;
import com.grahambartley.toomanychests.operation.WithdrawService;
import com.grahambartley.toomanychests.sort.ContainerSortService;
import com.grahambartley.toomanychests.sort.SortOutcome;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class RollbackSafetyGameTest implements FabricGameTest {
  private static final String BATCH = "rollback-safety";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;
  private static final Item TRACKED = Items.DIAMOND;

  private static final BlockPos LONE_SHULKER = new BlockPos(0, 1, 0);

  private static final int DEPOSIT_SLOT = 3;

  private final WithdrawService withdrawals = new WithdrawService();
  private final DepositService deposits = new DepositService();
  private final ContainerSortService sorts = new ContainerSortService();

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aDepositIntoANetworkThatFillsUpPartWayConservesEverything(TestContext context) {
    BarrelBlockEntity barrel = nearlyFullBarrel(context, STACK - 4);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(DEPOSIT_SLOT, new ItemStack(TRACKED, STACK));
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    DepositOutcome outcome =
        deposits.deposit(
            player,
            DepositRequestPayload.wholeSlot(
                NetworkOperationFixture.handlerOf(player).syncId, DEPOSIT_SLOT));

    context.assertTrue(
        outcome.result() == DepositResult.PARTIAL,
        "Four spaces for sixty-four items should report as partial, got " + outcome.result());
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, TRACKED) == STACK,
        "The barrel should have filled to the brim and no further");
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aDepositAllIntoAFullNetworkLeavesTheInventoryUntouched(TestContext context) {
    NetworkOperationFixture.fullBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(DEPOSIT_SLOT, new ItemStack(TRACKED, STACK));
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        outcome.result() == DepositResult.NO_SPACE,
        "A full network should report no space, got " + outcome.result());
    context.assertTrue(
        player.getInventory().getStack(DEPOSIT_SLOT).getCount() == STACK,
        "The stack should still be in the slot it started in");
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aDepositAcrossContainersWithOneRemovedConservesEverything(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    BarrelBlockEntity surviving =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(DEPOSIT_SLOT, new ItemStack(TRACKED, STACK));
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    context.setBlockState(NetworkOperationFixture.EAST_BARREL, Blocks.AIR);
    deposits.deposit(
        player,
        DepositRequestPayload.wholeSlot(
            NetworkOperationFixture.handlerOf(player).syncId, DEPOSIT_SLOT));

    context.assertTrue(
        NetworkOperationFixture.countIn(surviving, TRACKED) == STACK,
        "The whole stack should have landed in the container still standing, found "
            + NetworkOperationFixture.countIn(surviving, TRACKED));
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aSortThatCannotBeCompletedPutsBackEverythingItTook(TestContext context) {
    ShulkerBoxBlockEntity box = shulkerBoxRefusingItsOwnContents(context);
    ServerPlayerEntity player = NetworkOperationFixture.standingPlayer(context);
    context.useBlock(LONE_SHULKER, player);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    SortOutcome outcome = sorts.sort(player, player.currentScreenHandler.syncId);

    context.assertTrue(
        outcome == SortOutcome.FAILED,
        "A sort that cannot refill the container should fail, got " + outcome);
    context.assertTrue(
        box.getStack(0).isOf(Items.SHULKER_BOX) && box.getStack(1).isOf(TRACKED),
        "A failed sort should have left every slot exactly as it was");
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalThroughATerminalUnpoweredMidRequestMovesNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, TRACKED, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    context.setBlockState(NetworkOperationFixture.POWER, Blocks.AIR);
    TerminalNetworks.power()
        .signalChanged(
            context.getWorld(),
            context.getAbsolutePos(NetworkOperationFixture.TERMINAL),
            context.getBlockState(NetworkOperationFixture.TERMINAL));
    WithdrawOutcome outcome = take(player, STACK);

    context.assertTrue(
        outcome.result() == WithdrawResult.REFUSED,
        "An unpowered terminal should refuse, got " + outcome.result());
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalThroughATerminalBrokenMidRequestMovesNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, TRACKED, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    context.setBlockState(NetworkOperationFixture.TERMINAL, Blocks.AIR);
    WithdrawOutcome outcome = take(player, STACK);

    context.assertTrue(
        outcome.result() == WithdrawResult.REFUSED,
        "A terminal that is no longer there should refuse, got " + outcome.result());
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  private WithdrawOutcome take(ServerPlayerEntity player, int amount) {
    return withdrawals.withdraw(
        player,
        new WithdrawRequestPayload(
            NetworkOperationFixture.handlerOf(player).syncId,
            ItemVariant.of(TRACKED),
            amount,
            WithdrawDestination.CURSOR));
  }

  private static BarrelBlockEntity nearlyFullBarrel(TestContext context, int alreadyHeld) {
    final BarrelBlockEntity barrel =
        NetworkOperationFixture.fullBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE);
    barrel.setStack(barrel.size() - 1, new ItemStack(TRACKED, alreadyHeld));
    barrel.markDirty();
    return barrel;
  }

  private static ShulkerBoxBlockEntity shulkerBoxRefusingItsOwnContents(TestContext context) {
    context.setBlockState(LONE_SHULKER, Blocks.SHULKER_BOX.getDefaultState());
    final ShulkerBoxBlockEntity box = context.getBlockEntity(LONE_SHULKER);
    box.setStack(0, new ItemStack(Items.SHULKER_BOX, 1));
    box.setStack(1, new ItemStack(TRACKED, 5));
    box.markDirty();
    return box;
  }
}
