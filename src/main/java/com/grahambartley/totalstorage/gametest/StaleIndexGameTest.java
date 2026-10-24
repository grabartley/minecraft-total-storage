package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.network.WithdrawRequestPayload;
import com.grahambartley.totalstorage.operation.WithdrawDestination;
import com.grahambartley.totalstorage.operation.WithdrawOutcome;
import com.grahambartley.totalstorage.operation.WithdrawResult;
import com.grahambartley.totalstorage.operation.WithdrawService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class StaleIndexGameTest implements FabricGameTest {
  private static final String BATCH = "stale-index";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  private static final int DRAINABLE = 16;

  private static final int RACE_TICK_LIMIT = 120;
  private static final int FIRST_CLICK_TICK = 5;
  private static final int LAST_CLICK_TICK = 60;
  private static final int VERDICT_TICK = 80;

  private static final BlockPos RAISED_BARREL = new BlockPos(2, 2, 3);
  private static final BlockPos DRAINING_HOPPER = new BlockPos(2, 1, 3);
  private static final BlockPos SINK_CHEST = new BlockPos(1, 1, 3);

  private final WithdrawService withdrawals = new WithdrawService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void askingForMoreThanRemainsGrantsWhatExistsAndReportsTheShortfall(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 10);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    WithdrawOutcome outcome = take(player, STACK);

    context.assertTrue(
        outcome.result() == WithdrawResult.PARTIAL_NO_STOCK,
        "Ten of sixty-four should report as partial on stock, got " + outcome.result());
    context.assertTrue(
        outcome.moved() == 10L, "It granted " + outcome.moved() + " rather than ten");
    context.assertTrue(
        outcome.shortfall() == STACK - 10L,
        "The shortfall was " + outcome.shortfall() + " rather than " + (STACK - 10));
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anItemRemovedInTheSameTickGrantsNothingAndResyncsTheScreen(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity player =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));
    long before = NetworkOperationFixture.handlerOf(player).listing().revision();

    barrel.setStack(0, ItemStack.EMPTY);
    barrel.markDirty();
    WithdrawOutcome outcome = take(player, STACK);

    context.assertTrue(
        outcome.result() == WithdrawResult.NOTHING_AVAILABLE,
        "An item that has gone should grant nothing, got " + outcome.result());
    context.assertTrue(
        NetworkOperationFixture.handlerOf(player).listing().revision() > before,
        "The screen that asked for something gone should have been put back in step");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anEntryNoContainerCanHonourReadsAsGoneRatherThanAsNoRoom(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity player =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));

    barrel.setStack(0, ItemStack.EMPTY);
    WithdrawOutcome outcome = take(player, STACK);

    context.assertTrue(
        outcome.result() == WithdrawResult.NOTHING_AVAILABLE,
        "A stale entry should read as an item that has gone, got " + outcome.result());
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anItemTheNetworkStillHoldsWithNowhereToPutItReadsAsNoRoom(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.currentScreenHandler.setCursorStack(new ItemStack(Items.COBBLESTONE, STACK));

    WithdrawOutcome outcome = takeOntoPointer(player, STACK);

    context.assertTrue(
        outcome.result() == WithdrawResult.NO_ROOM,
        "A full pointer should read as nowhere to put it, got " + outcome.result());
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerBrokenBetweenTheListingAndTheClickGrantsNothingAndResyncs(
      TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity player =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));
    long before = NetworkOperationFixture.handlerOf(player).listing().revision();

    context.setBlockState(NetworkOperationFixture.EAST_BARREL, Blocks.AIR);
    WithdrawOutcome outcome = take(player, STACK);

    context.assertTrue(
        outcome.moved() == 0L, "A broken container granted " + outcome.moved() + " items");
    context.assertTrue(
        NetworkOperationFixture.handlerOf(player).listing().revision() > before,
        "The screen still offering a container that has gone was not put back in step");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = RACE_TICK_LIMIT)
  public void clickingWhileAHopperDrainsTheContainerNeverGrantsMoreThanItHeld(TestContext context) {
    drainingBarrel(context);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, Items.DIAMOND, player);

    for (int tick = FIRST_CLICK_TICK; tick <= LAST_CLICK_TICK; tick++) {
      context.runAtTick(tick, () -> take(player, 1));
    }

    context.runAtTick(
        VERDICT_TICK,
        () -> {
          final long carried = NetworkOperationFixture.countCarried(player, Items.DIAMOND);
          context.assertTrue(
              carried > 0L, "The clicks granted nothing at all, so nothing raced the hopper");
          context.assertTrue(
              carried <= DRAINABLE,
              "The player was granted " + carried + " of a barrel holding " + DRAINABLE);
          final long after = NetworkOperationFixture.countInWorld(context, Items.DIAMOND, player);
          context.assertTrue(
              after == before, "The world held " + before + " diamonds and now holds " + after);
          NetworkOperationFixture.finish(context, player);
        });
  }

  private static void drainingBarrel(TestContext context) {
    NetworkOperationFixture.stockedBarrel(context, RAISED_BARREL, Items.DIAMOND, DRAINABLE);
    context.setBlockState(
        DRAINING_HOPPER, Blocks.HOPPER.getDefaultState().with(HopperBlock.FACING, Direction.WEST));
    context.setBlockState(SINK_CHEST, Blocks.CHEST.getDefaultState());
  }

  private WithdrawOutcome take(ServerPlayerEntity player, int amount) {
    return take(player, amount, WithdrawDestination.INVENTORY);
  }

  private WithdrawOutcome takeOntoPointer(ServerPlayerEntity player, int amount) {
    return take(player, amount, WithdrawDestination.CURSOR);
  }

  private WithdrawOutcome take(
      ServerPlayerEntity player, int amount, WithdrawDestination destination) {
    return withdrawals.withdraw(
        player,
        new WithdrawRequestPayload(
            NetworkOperationFixture.handlerOf(player).syncId,
            ItemVariant.of(Items.DIAMOND),
            amount,
            destination));
  }
}
