package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.lock.LockedSlots;
import com.grahambartley.totalstorage.lock.LockedSlotsAttachment;
import com.grahambartley.totalstorage.network.DepositRequestPayload;
import com.grahambartley.totalstorage.operation.DepositOutcome;
import com.grahambartley.totalstorage.operation.DepositResult;
import com.grahambartley.totalstorage.operation.DepositService;
import com.grahambartley.totalstorage.operation.OperationGuards;
import com.grahambartley.totalstorage.storage.ContainerAssignment;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.storage.ContainerRole;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class DepositServiceGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-deposit";

  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  private static final int HELMET_SLOT = 39;

  private static final int OFFHAND_SLOT = 40;

  private final DepositService deposits = new DepositService();

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aStackFromAnInventorySlotIsStoredInTheNetwork(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(3, new ItemStack(Items.DIAMOND, STACK));

    DepositOutcome outcome =
        deposits.deposit(
            player,
            DepositRequestPayload.wholeSlot(NetworkOperationFixture.handlerOf(player).syncId, 3));

    context.assertTrue(
        outcome.result() == DepositResult.DEPOSITED, "The stack should have been stored");
    context.assertTrue(
        player.getInventory().getStack(3).isEmpty(), "The slot should have been emptied");
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND) == STACK,
        "The barrel should hold the stack");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aStackOnThePointerIsStoredInTheNetwork(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.currentScreenHandler.setCursorStack(new ItemStack(Items.DIAMOND, STACK));

    deposits.deposit(
        player,
        DepositRequestPayload.wholeCursor(NetworkOperationFixture.handlerOf(player).syncId));

    context.assertTrue(
        player.currentScreenHandler.getCursorStack().isEmpty(),
        "The pointer should have been emptied");
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND) == STACK,
        "The barrel should hold the stack");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void oneFromThePointerLeavesTheRestOfTheStackInHand(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.currentScreenHandler.setCursorStack(new ItemStack(Items.DIAMOND, STACK));

    deposits.deposit(
        player,
        DepositRequestPayload.oneFromCursor(NetworkOperationFixture.handlerOf(player).syncId));

    context.assertTrue(
        player.currentScreenHandler.getCursorStack().getCount() == STACK - 1,
        "The pointer holds " + player.currentScreenHandler.getCursorStack().getCount());
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND) == 1L,
        "The barrel holds "
            + NetworkOperationFixture.countIn(barrel, Items.DIAMOND)
            + " rather than the one that left the pointer");
    long now =
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND)
            + NetworkOperationFixture.countCarried(player, Items.DIAMOND);
    context.assertTrue(
        now == STACK, "The world started with " + STACK + " diamonds and now holds " + now);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void depositAllEmptiesEverythingExceptLockedSlotsArmourAndTheOffhand(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    for (int slot = LockedSlots.FIRST_SLOT; slot <= LockedSlots.LAST_SLOT; slot++) {
      player.getInventory().setStack(slot, new ItemStack(Items.COBBLESTONE, 8));
    }
    player.getInventory().setStack(HELMET_SLOT, new ItemStack(Items.DIAMOND_HELMET));
    player.getInventory().setStack(OFFHAND_SLOT, new ItemStack(Items.SHIELD));
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(2, 17)));

    deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        player.getInventory().getStack(2).getCount() == 8,
        "A locked slot was emptied by deposit all");
    context.assertTrue(
        player.getInventory().getStack(17).getCount() == 8,
        "A locked slot was emptied by deposit all");
    context.assertTrue(
        player.getInventory().getStack(0).isEmpty(), "An unlocked slot should have been emptied");
    context.assertTrue(
        player.getInventory().getStack(HELMET_SLOT).isOf(Items.DIAMOND_HELMET),
        "Armour was deposited");
    context.assertTrue(
        player.getInventory().getStack(OFFHAND_SLOT).isOf(Items.SHIELD),
        "The offhand was deposited");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aShiftClickOutOfALockedSlotMovesItTheSameAsIntoAChest(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(5, new ItemStack(Items.DIAMOND, STACK));
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(5)));

    DepositOutcome outcome =
        deposits.deposit(
            player,
            DepositRequestPayload.wholeSlot(NetworkOperationFixture.handlerOf(player).syncId, 5));

    context.assertTrue(
        outcome.result() == DepositResult.DEPOSITED,
        "The player chose the slot, so the lock should not have stopped them, got "
            + outcome.result());
    context.assertTrue(
        player.getInventory().getStack(5).isEmpty(),
        "The stack the player shift-clicked stayed where it was");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void depositAllStillLeavesALockedSlotAlone(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(5, new ItemStack(Items.DIAMOND, STACK));
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(5)));

    deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        player.getInventory().getStack(5).getCount() == STACK, "Deposit all emptied a locked slot");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedContainerNeverReceivesADeposit(TestContext context) {
    BarrelBlockEntity excluded =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context, NetworkOperationFixture.EAST_BARREL, ContainerRole.EXCLUDED);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));

    DepositOutcome outcome =
        deposits.deposit(
            player,
            DepositRequestPayload.wholeSlot(NetworkOperationFixture.handlerOf(player).syncId, 0));

    context.assertTrue(
        outcome.result() == DepositResult.NO_SPACE,
        "An excluded container took the deposit, got " + outcome.result());
    context.assertTrue(
        NetworkOperationFixture.countIn(excluded, Items.DIAMOND) == 0L,
        "An excluded container was written to");
    context.assertTrue(
        player.getInventory().getStack(0).getCount() == STACK,
        "The player's stack should be exactly where it was");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFilteredContainerIsPreferredOverAGeneralOne(TestContext context) {
    BarrelBlockEntity general =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    BarrelBlockEntity filtered =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context,
        NetworkOperationFixture.WEST_BARREL,
        ContainerAssignment.filtered(ContainerFilter.ofItems("minecraft:diamond")));
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));

    deposits.deposit(
        player,
        DepositRequestPayload.wholeSlot(NetworkOperationFixture.handlerOf(player).syncId, 0));

    context.assertTrue(
        NetworkOperationFixture.countIn(filtered, Items.DIAMOND) == STACK,
        "The filtered container should have taken the diamonds");
    context.assertTrue(
        NetworkOperationFixture.countIn(general, Items.DIAMOND) == 0L,
        "The general container took what a filtered one had asked for");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFullFilteredContainerFallsThroughToAGeneralOne(TestContext context) {
    BarrelBlockEntity general =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    NetworkOperationFixture.fullBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context,
        NetworkOperationFixture.WEST_BARREL,
        ContainerAssignment.filtered(ContainerFilter.ofItems("minecraft:diamond")));
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));

    deposits.deposit(
        player,
        DepositRequestPayload.wholeSlot(NetworkOperationFixture.handlerOf(player).syncId, 0));

    context.assertTrue(
        NetworkOperationFixture.countIn(general, Items.DIAMOND) == STACK,
        "A full filtered container should not have refused the item outright");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFullNetworkLeavesTheInventoryUntouchedAndSaysSo(TestContext context) {
    NetworkOperationFixture.fullBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        outcome.result() == DepositResult.NO_SPACE,
        "A full network should say it has no room, got " + outcome.result());
    context.assertTrue(
        player.getInventory().getStack(0).getCount() == STACK,
        "The player's inventory should be exactly as it was");
    context.assertTrue(
        context
            .getWorld()
            .getEntitiesByClass(ItemEntity.class, context.getTestBox(), entity -> true)
            .isEmpty(),
        "A refused deposit must never drop items on the floor");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNetworkWithPartialRoomStoresWhatFitsAndConservesTheRest(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.fullBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE);
    barrel.setStack(0, new ItemStack(Items.COBBLESTONE, 44));
    barrel.markDirty();
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, STACK));
    long before =
        NetworkOperationFixture.countIn(barrel, Items.COBBLESTONE)
            + NetworkOperationFixture.countCarried(player, Items.COBBLESTONE);

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        outcome.result() == DepositResult.PARTIAL,
        "A network with room for some of it should report a partial deposit, got "
            + outcome.result());
    long carried = NetworkOperationFixture.countCarried(player, Items.COBBLESTONE);
    long stored = NetworkOperationFixture.countIn(barrel, Items.COBBLESTONE);
    context.assertTrue(
        carried + stored == before,
        "The world started with " + before + " cobblestone and now holds " + (carried + stored));
    context.assertTrue(
        outcome.remaining() == carried,
        "The report said " + outcome.remaining() + " was left but the player holds " + carried);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void havingNothingToDepositIsToldApartFromHavingNowhereToPutIt(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        outcome.result() == DepositResult.NOTHING_TO_DEPOSIT,
        "An empty inventory should say there is nothing to store, got " + outcome.result());
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anIndexNoLockableSlotHasIsRefusedRatherThanActedOn(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));
    player.getInventory().setStack(HELMET_SLOT, new ItemStack(Items.DIAMOND_HELMET));
    int syncId = NetworkOperationFixture.handlerOf(player).syncId;

    for (final int forged :
        new int[] {-1, Integer.MIN_VALUE, LockedSlots.SLOT_COUNT, HELMET_SLOT, OFFHAND_SLOT}) {
      DepositOutcome outcome =
          deposits.deposit(player, DepositRequestPayload.wholeSlot(syncId, forged));
      context.assertTrue(
          outcome.result() == DepositResult.REFUSED,
          "Index " + forged + " was acted on, giving " + outcome.result());
    }

    context.assertTrue(
        player.getInventory().getStack(0).getCount() == STACK,
        "A forged index was clamped onto the first hotbar slot");
    context.assertTrue(
        player.getInventory().getStack(HELMET_SLOT).isOf(Items.DIAMOND_HELMET),
        "Armour was reachable by naming its own inventory index");
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND) == 0L,
        "A refused deposit stored items anyway");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aDepositNamingAnotherScreenIsRefused(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));

    DepositOutcome outcome =
        deposits.deposit(
            player,
            DepositRequestPayload.wholeSlot(
                NetworkOperationFixture.handlerOf(player).syncId + 1, 0));

    context.assertTrue(
        outcome.result() == DepositResult.REFUSED,
        "A request naming another screen should be refused, got " + outcome.result());
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.DIAMOND) == 0L,
        "A refused deposit stored items anyway");
    NetworkOperationFixture.finish(context, player);
  }
}
