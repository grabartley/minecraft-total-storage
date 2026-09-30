package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.operation.OperationMessages;
import com.grahambartley.toomanychests.operation.WithdrawAmount;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.operation.WithdrawOutcome;
import com.grahambartley.toomanychests.operation.WithdrawResult;
import com.grahambartley.toomanychests.operation.WithdrawService;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import io.netty.buffer.Unpooled;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class WithdrawServiceGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-withdraw";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  private final WithdrawService withdrawals = new WithdrawService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aStackWithdrawalPutsAWholeStackOnThePointer(TestContext context) {
    BarrelBlockEntity barrel = stocked(context, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    WithdrawOutcome outcome = take(player, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(
        outcome.result() == WithdrawResult.WITHDRAWN, "A stack should have been withdrawn");
    context.assertTrue(
        cursorCount(player) == STACK, "The pointer holds " + cursorCount(player) + " diamonds");
    assertConserved(context, barrel, player, STACK);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aHalfStackWithdrawalTakesHalfAndLeavesTheRest(TestContext context) {
    BarrelBlockEntity barrel = stocked(context, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    take(player, STACK / 2, WithdrawDestination.CURSOR);

    context.assertTrue(
        cursorCount(player) == STACK / 2, "The pointer holds " + cursorCount(player) + " diamonds");
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND) == STACK / 2,
        "The barrel should keep the other half");
    assertConserved(context, barrel, player, STACK);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalToTheInventoryLandsInThePlayersOwnSlots(TestContext context) {
    BarrelBlockEntity barrel = stocked(context, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    take(player, STACK, WithdrawDestination.INVENTORY);

    context.assertTrue(
        player.currentScreenHandler.getCursorStack().isEmpty(),
        "Nothing should have been put on the pointer");
    context.assertTrue(
        NetworkOperationFixture.countCarried(player, Items.DIAMOND) == STACK,
        "The player should be carrying the stack");
    assertConserved(context, barrel, player, STACK);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void askingForMoreThanTheNetworkHoldsTakesEverythingAndReportsTheShortfall(
      TestContext context) {
    BarrelBlockEntity barrel = stocked(context, 9);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    WithdrawOutcome outcome = take(player, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(
        outcome.result() == WithdrawResult.PARTIAL_NO_STOCK,
        "The withdrawal should report as partial on stock, got " + outcome.result());
    context.assertTrue(outcome.moved() == 9L, "It took " + outcome.moved() + " rather than nine");
    context.assertTrue(
        outcome.shortfall() == STACK - 9L,
        "The shortfall was " + outcome.shortfall() + " rather than " + (STACK - 9));
    assertConserved(context, barrel, player, 9);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalForSomethingTheNetworkNeverHeldGrantsNothing(TestContext context) {
    stocked(context, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    WithdrawOutcome outcome =
        withdrawals.withdraw(
            player,
            new WithdrawRequestPayload(
                NetworkOperationFixture.handlerOf(player).syncId,
                ItemVariant.of(Items.NETHERITE_INGOT),
                STACK,
                WithdrawDestination.CURSOR));

    context.assertTrue(
        outcome.result() == WithdrawResult.NOTHING_AVAILABLE,
        "The network holds none of it, got " + outcome.result());
    context.assertTrue(
        NetworkOperationFixture.countCarried(player, Items.NETHERITE_INGOT) == 0L,
        "Nothing should have been conjured");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalAgainstAContainerBrokenInTheSameTickGrantsNothing(TestContext context) {
    stocked(context, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    context.setBlockState(NetworkOperationFixture.EAST_BARREL, Blocks.AIR);

    WithdrawOutcome outcome = take(player, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(
        outcome.result() == WithdrawResult.NOTHING_AVAILABLE,
        "A container that has gone leaves the network holding none of it, got " + outcome.result());
    context.assertTrue(
        NetworkOperationFixture.countCarried(player, Items.DIAMOND) == 0L,
        "The player was handed items from a container that no longer exists");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void twoPlayersTakingTheLastStackTogetherProduceExactlyOneSuccess(TestContext context) {
    BarrelBlockEntity barrel = stocked(context, STACK);
    ServerPlayerEntity first = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity second = NetworkOperationFixture.alsoOpen(context);

    WithdrawOutcome won = take(first, STACK, WithdrawDestination.CURSOR);
    WithdrawOutcome lost = take(second, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(won.moved() == STACK, "The first player should have taken the stack");
    context.assertTrue(
        lost.result() == WithdrawResult.NOTHING_AVAILABLE,
        "The loser of the race should find the network empty, got " + lost.result());
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND) == 0L,
        "The barrel should be empty rather than owing items");
    context.assertTrue(
        NetworkOperationFixture.countCarried(first, Items.DIAMOND)
                + NetworkOperationFixture.countCarried(second, Items.DIAMOND)
            == STACK,
        "One stack went in and more than one came out");
    NetworkOperationFixture.finish(context, first, second);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aLockedSlotIsNeverUsedAsADestinationForAWithdrawal(TestContext context) {
    stocked(context, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    fillEverySlotExcept(player, 4);
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(4)));

    WithdrawOutcome outcome = take(player, STACK, WithdrawDestination.INVENTORY);

    context.assertFalse(
        outcome.result().movedAnything(),
        "A locked slot was filled, taking " + outcome.moved() + " diamonds");
    context.assertTrue(
        player.getInventory().getStack(4).isEmpty(), "The locked slot should still be empty");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFullInventoryTakesWhatFitsAndLeavesTheRestInTheNetwork(TestContext context) {
    BarrelBlockEntity barrel = stocked(context, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    fillEverySlotExcept(player, 4);
    player.getInventory().setStack(4, new ItemStack(Items.DIAMOND, STACK - 10));

    WithdrawOutcome outcome = take(player, STACK, WithdrawDestination.INVENTORY);

    context.assertTrue(outcome.moved() == 10L, "Only ten diamonds fit, moved " + outcome.moved());
    context.assertTrue(
        outcome.result() == WithdrawResult.PARTIAL_NO_ROOM,
        "A shortfall the player had no room for should say so, got " + outcome.result());
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND) == STACK - 10L,
        "The rest should still be in the network");
    context.assertTrue(
        context
            .getWorld()
            .getEntitiesByClass(
                net.minecraft.entity.ItemEntity.class, context.getTestBox(), entity -> true)
            .isEmpty(),
        "A withdrawal must never drop items on the floor");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void everythingARequestNamedSurvivesTheWireUnchanged(TestContext context) {
    RegistryByteBuf buffer =
        new RegistryByteBuf(Unpooled.buffer(), context.getWorld().getRegistryManager());
    WithdrawRequestPayload sent =
        new WithdrawRequestPayload(
            7, ItemVariant.of(Items.DIAMOND), 32, WithdrawDestination.INVENTORY);

    WithdrawRequestPayload.CODEC.encode(buffer, sent);
    WithdrawRequestPayload received = WithdrawRequestPayload.CODEC.decode(buffer);

    context.assertTrue(sent.equals(received), "The request came back as " + received);
    context.assertTrue(buffer.readableBytes() == 0, "The payload left bytes on the wire");
    context.complete();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void clickingACellHoldingLessThanAStackTakesItAllWithNoNotice(TestContext context) {
    BarrelBlockEntity barrel = stocked(context, 48);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    WithdrawOutcome outcome =
        take(player, WithdrawAmount.stackFrom(STACK, 48L), WithdrawDestination.CURSOR);

    context.assertTrue(
        outcome.result() == WithdrawResult.WITHDRAWN,
        "Taking what the cell showed is not a shortfall, got " + outcome.result());
    context.assertTrue(
        OperationMessages.forWithdrawal(outcome).isEmpty(),
        "Taking everything the cell showed should say nothing");
    context.assertTrue(cursorCount(player) == 48L, "The pointer holds " + cursorCount(player));
    assertConserved(context, barrel, player, 48);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void rightClickingACellOfFortyEightTakesTwentyFourWithNoNotice(TestContext context) {
    BarrelBlockEntity barrel = stocked(context, 48);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    WithdrawOutcome outcome =
        take(player, WithdrawAmount.halfStackFrom(STACK, 48L), WithdrawDestination.CURSOR);

    context.assertTrue(
        OperationMessages.forWithdrawal(outcome).isEmpty(), "Half of the cell should say nothing");
    context.assertTrue(cursorCount(player) == 24L, "The pointer holds " + cursorCount(player));
    assertConserved(context, barrel, player, 48);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void repeatedShiftClicksOnALargeCellEachMoveOneStackWithNoNotice(TestContext context) {
    final int held = 4 * STACK;
    BarrelBlockEntity barrel = stocked(context, STACK);
    for (int slot = 1; slot < 4; slot++) {
      barrel.setStack(slot, new ItemStack(Items.DIAMOND, STACK));
    }
    barrel.markDirty();
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    for (int click = 1; click <= 3; click++) {
      final long showing = held - (long) (click - 1) * STACK;
      WithdrawOutcome outcome =
          take(player, WithdrawAmount.stackFrom(STACK, showing), WithdrawDestination.INVENTORY);
      context.assertTrue(
          outcome.result() == WithdrawResult.WITHDRAWN && outcome.moved() == STACK,
          "Shift-click " + click + " moved " + outcome.moved() + " as " + outcome.result());
      context.assertTrue(
          OperationMessages.forWithdrawal(outcome).isEmpty(),
          "Shift-click " + click + " should say nothing");
    }

    context.assertTrue(
        NetworkOperationFixture.countCarried(player, Items.DIAMOND) == 3L * STACK,
        "Three shift-clicks should carry three stacks");
    assertConserved(context, barrel, player, held);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void clickingACellThatShowedMoreThanTheNetworkStillHoldsReportsTheShortfall(
      TestContext context) {
    BarrelBlockEntity barrel = stocked(context, 30);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    WithdrawOutcome outcome =
        take(player, WithdrawAmount.stackFrom(STACK, 48L), WithdrawDestination.CURSOR);

    context.assertTrue(
        outcome.result() == WithdrawResult.PARTIAL_NO_STOCK,
        "A stale cell should fall short on stock, got " + outcome.result());
    context.assertTrue(
        OperationMessages.forWithdrawal(outcome).isPresent(),
        "A stale cell's shortfall should be reported");
    context.assertTrue(cursorCount(player) == 30L, "The pointer holds " + cursorCount(player));
    assertConserved(context, barrel, player, 30);
    NetworkOperationFixture.finish(context, player);
  }

  private WithdrawOutcome take(
      ServerPlayerEntity player, int amount, WithdrawDestination destination) {
    final TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(player);
    return withdrawals.withdraw(
        player,
        new WithdrawRequestPayload(
            handler.syncId, ItemVariant.of(Items.DIAMOND), amount, destination));
  }

  private static BarrelBlockEntity stocked(TestContext context, int diamonds) {
    return NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, diamonds);
  }

  private static long cursorCount(ServerPlayerEntity player) {
    final ItemStack cursor = player.currentScreenHandler.getCursorStack();
    return cursor.isOf(Items.DIAMOND) ? cursor.getCount() : 0L;
  }

  private static void fillEverySlotExcept(ServerPlayerEntity player, int spare) {
    for (int slot = LockedSlots.FIRST_SLOT; slot <= LockedSlots.LAST_SLOT; slot++) {
      player
          .getInventory()
          .setStack(slot, slot == spare ? ItemStack.EMPTY : new ItemStack(Items.STONE, 64));
    }
  }

  private static void assertConserved(
      TestContext context, BarrelBlockEntity barrel, ServerPlayerEntity player, long started) {
    final long now =
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND)
            + NetworkOperationFixture.countCarried(player, Items.DIAMOND);
    context.assertTrue(
        now == started, "The world started with " + started + " diamonds and now holds " + now);
  }
}
