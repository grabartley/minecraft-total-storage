package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.operation.WithdrawOutcome;
import com.grahambartley.toomanychests.operation.WithdrawResult;
import com.grahambartley.toomanychests.operation.WithdrawService;
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

/**
 * What happens when the client's picture of the network has already stopped being true.
 *
 * <p>Per ADR 0009 that gap is permanent: a page is built, sent, drawn, and clicked, and the network
 * is free to change throughout. Every case here is an ordinary race with a defined answer rather
 * than a fault, so what is proven is that each one is answered quietly, correctly, and with the
 * right reason, and that none of them ever fabricates an item to satisfy the request.
 *
 * <p>The reason matters as much as the amount. A player told their inventory is full when the item
 * had simply gone will go and empty a slot that was never the problem, so the two empty-handed
 * endings are told apart here against real containers.
 */
public final class StaleIndexGameTest implements FabricGameTest {
  private static final String BATCH = "stale-index";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  /** Enough for a hopper to still be draining while the clicks are landing. */
  private static final int DRAINABLE = 16;

  private static final int RACE_TICK_LIMIT = 120;
  private static final int FIRST_CLICK_TICK = 5;
  private static final int LAST_CLICK_TICK = 60;
  private static final int VERDICT_TICK = 80;

  private static final BlockPos RAISED_BARREL = new BlockPos(2, 2, 3);
  private static final BlockPos DRAINING_HOPPER = new BlockPos(2, 1, 3);
  private static final BlockPos SINK_CHEST = new BlockPos(1, 1, 3);

  private final WithdrawService withdrawals = new WithdrawService();

  /** Ten left where the listing offered sixty-four: take the ten and say so. */
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
        outcome.result() == WithdrawResult.PARTIAL,
        "Ten of sixty-four should report as partial, got " + outcome.result());
    context.assertTrue(
        outcome.moved() == 10L, "It granted " + outcome.moved() + " rather than ten");
    context.assertTrue(
        outcome.shortfall() == STACK - 10L,
        "The shortfall was " + outcome.shortfall() + " rather than " + (STACK - 10));
    NetworkOperationFixture.finish(context, player);
  }

  /** An entry the network can no longer honour is answered with nothing and a fresh page. */
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

  /**
   * The index still names a container that no longer holds the item, which is what a mutation
   * nobody announced leaves behind. The request must read as an item that has gone rather than as
   * an inventory with no room, because those send the player to fix two different things.
   */
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

  /** The other empty-handed ending: the network has it and the player has nowhere to put it. */
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

  /** A container broken between the page and the click grants nothing and resyncs. */
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

  /**
   * A player clicking one item per tick while a hopper empties the same container out of the
   * network underneath them. Whatever order the two land in, what leaves the barrel has to arrive
   * somewhere exactly once.
   */
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
              carried <= DRAINABLE,
              "The player was granted " + carried + " of a barrel holding " + DRAINABLE);
          final long after = NetworkOperationFixture.countInWorld(context, Items.DIAMOND, player);
          context.assertTrue(
              after == before, "The world held " + before + " diamonds and now holds " + after);
          NetworkOperationFixture.finish(context, player);
        });
  }

  /**
   * A barrel inside the network with a hopper under it pushing into a chest outside, so the network
   * really loses items rather than shuffling them between its own containers.
   */
  private static void drainingBarrel(TestContext context) {
    NetworkOperationFixture.stockedBarrel(context, RAISED_BARREL, Items.DIAMOND, DRAINABLE);
    context.setBlockState(
        DRAINING_HOPPER, Blocks.HOPPER.getDefaultState().with(HopperBlock.FACING, Direction.WEST));
    context.setBlockState(SINK_CHEST, Blocks.CHEST.getDefaultState());
  }

  private WithdrawOutcome take(ServerPlayerEntity player, int amount) {
    return take(player, amount, WithdrawDestination.INVENTORY);
  }

  /** The pointer holds one stack and nothing else, which is how a destination is made full. */
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
