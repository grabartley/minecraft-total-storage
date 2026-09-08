package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ConfigBuilder;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.network.DepositRequestPayload;
import com.grahambartley.toomanychests.operation.DepositOutcome;
import com.grahambartley.toomanychests.operation.DepositResult;
import com.grahambartley.toomanychests.operation.DepositService;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.PlayerSlots;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerFilter;
import com.grahambartley.toomanychests.storage.ContainerRole;
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

/**
 * Putting items into a network, against real containers.
 *
 * <p>The routing order is exhausted in JUnit against plain data. What only a world can show is that
 * the roles a terminal really holds reach that decision, that the stack really leaves the player's
 * hands, and that the promises made when it cannot are kept: nothing on the floor, locked slots
 * untouched, and a full network leaving the inventory exactly as it was.
 */
public final class DepositServiceGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-deposit";

  /**
   * The cooldown and the hotbar setting are server-wide, and batches run in parallel. Anything that
   * reads or writes one of them runs here instead, so no other batch can change the value between a
   * test setting it and reading it back.
   */
  private static final String GUARDS_BATCH = "terminal-deposit-guards";

  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  /** Armour and the offhand sit above the lockable range and are never deposited. */
  private static final int HELMET_SLOT = 39;

  private static final int OFFHAND_SLOT = 40;

  private final DepositService deposits = new DepositService();

  /**
   * Cooldowns are JVM-global and these tests share a batch, so a deposit in one would put the next
   * on cooldown and fail it for a reason that has nothing to do with what it is testing.
   */
  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @BeforeBatch(batchId = GUARDS_BATCH)
  public void clearGuardCooldowns(ServerWorld world) {
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

  /**
   * That the operator's hotbar setting really reaches the gesture. Which slots the setting names is
   * proven in {@code PlayerSlotsGameTest} without touching server-wide state; what this adds is
   * that the deposit reads the setting at all.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = GUARDS_BATCH,
      tickLimit = TICK_LIMIT)
  public void depositAllReadsTheServersHotbarSetting(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 8));
    player.getInventory().setStack(PlayerSlots.HOTBAR_SLOTS, new ItemStack(Items.OAK_LOG, 8));
    TooManyChestsConfig original = ServerConfigService.current();
    withHotbarSkipped(context, true);

    deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);
    restore(context, original);

    context.assertTrue(
        player.getInventory().getStack(0).getCount() == 8,
        "The hotbar was emptied while the server asked for it to be kept");
    context.assertTrue(
        player.getInventory().getStack(PlayerSlots.HOTBAR_SLOTS).isEmpty(),
        "The main inventory should still have been emptied");
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * Asking again before the cooldown is up changes nothing at all, rather than half-working. The
   * inventory is what proves it: a rate limited deposit that still moved items would be a limit
   * that costs a player their stuff.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = GUARDS_BATCH,
      tickLimit = TICK_LIMIT)
  public void aSecondDepositAllInTheSameTickIsRefusedAndMovesNothing(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));
    int syncId = NetworkOperationFixture.handlerOf(player).syncId;

    deposits.depositAll(player, syncId);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, STACK));
    DepositOutcome second = deposits.depositAll(player, syncId);

    context.assertTrue(
        second.result() == DepositResult.RATE_LIMITED,
        "A second deposit in the same tick should be rate limited, got " + second.result());
    context.assertTrue(
        player.getInventory().getStack(0).getCount() == STACK,
        "A rate limited deposit must move nothing");
    context.assertTrue(
        NetworkOperationFixture.countIn(barrel, Items.COBBLESTONE) == 0L,
        "A rate limited deposit reached the containers anyway");
    NetworkOperationFixture.finish(context, player);
  }

  /** A cooldown of nothing is the limit switched off, which is what the setting's floor means. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = GUARDS_BATCH,
      tickLimit = TICK_LIMIT)
  public void aServerThatSetTheCooldownToNothingIsNeverRateLimited(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.DIAMOND, STACK));
    int syncId = NetworkOperationFixture.handlerOf(player).syncId;
    TooManyChestsConfig original = ServerConfigService.current();
    withCooldown(context, 0);

    deposits.depositAll(player, syncId);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, STACK));
    DepositOutcome second = deposits.depositAll(player, syncId);
    restore(context, original);

    context.assertTrue(
        second.result() != DepositResult.RATE_LIMITED,
        "A cooldown of nothing should never refuse, got " + second.result());
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * What could not be stored is named, not merely counted. "Sixty left over" reads as a failure
   * where "sixty cobblestone left over" reads as a chest that filled up, which is actionable.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void whatWouldNotFitIsReportedByItemRatherThanOnlyCounted(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 30));
    player.getInventory().setStack(1, new ItemStack(Items.OAK_LOG, 10));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    context.assertTrue(
        outcome.result() == DepositResult.NO_SPACE,
        "A full network should report no space, got " + outcome.result());
    context.assertTrue(
        outcome.leftoverKinds() == 2,
        "Two kinds stayed behind, reported " + outcome.leftoverKinds());
    context.assertTrue(
        outcome.leftovers().get(0).variant().isOf(Items.COBBLESTONE),
        "The largest leftover should be named first, got " + outcome.leftovers().get(0).variant());
    context.assertTrue(
        outcome.leftovers().get(0).amount() == 30L,
        "Counted " + outcome.leftovers().get(0).amount() + " of 30 cobblestone left behind");
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * The report has to match what is really still in the inventory, or it is worse than no report: a
   * player who is told the wrong thing goes looking for items that never moved.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theLeftoverReportMatchesWhatIsStillInTheInventory(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(0, new ItemStack(Items.COBBLESTONE, 12));

    DepositOutcome outcome =
        deposits.depositAll(player, NetworkOperationFixture.handlerOf(player).syncId);

    long reported =
        outcome.leftovers().stream()
            .filter(left -> left.variant().isOf(Items.COBBLESTONE))
            .mapToLong(left -> left.amount())
            .sum();
    context.assertTrue(
        reported == NetworkOperationFixture.countCarried(player, Items.COBBLESTONE),
        "Reported "
            + reported
            + " left over but the player holds "
            + NetworkOperationFixture.countCarried(player, Items.COBBLESTONE));
    NetworkOperationFixture.finish(context, player);
  }

  private static void withHotbarSkipped(TestContext context, boolean skip) {
    ServerConfigService.update(
        context.getWorld().getServer(),
        ConfigBuilder.from(ServerConfigService.current()).depositAllSkipsHotbar(skip).build());
  }

  private static void withCooldown(TestContext context, int ticks) {
    ServerConfigService.update(
        context.getWorld().getServer(),
        ConfigBuilder.from(ServerConfigService.current())
            .expensiveOperationCooldownTicks(ticks)
            .build());
  }

  /** Config is server-wide, so a test that changed it puts it back before the next one reads it. */
  private static void restore(TestContext context, TooManyChestsConfig original) {
    ServerConfigService.update(context.getWorld().getServer(), original);
  }

  /**
   * The headline gesture. ADR 0010 and the locking epic make the exclusions the point of it: a
   * deposit all that emptied a locked slot would make every lock in the mod worthless.
   */
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

  /** A forged deposit all cannot name a locked slot, because the payload names no slots at all. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRequestNamingALockedSlotIsRefusedRatherThanHonoured(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    player.getInventory().setStack(5, new ItemStack(Items.DIAMOND, STACK));
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(5)));

    DepositOutcome outcome =
        deposits.deposit(
            player,
            DepositRequestPayload.wholeSlot(NetworkOperationFixture.handlerOf(player).syncId, 5));

    context.assertTrue(
        outcome.result() == DepositResult.SLOT_LOCKED,
        "A locked slot was reachable, got " + outcome.result());
    context.assertTrue(
        player.getInventory().getStack(5).getCount() == STACK,
        "The locked slot was emptied by a forged request");
    NetworkOperationFixture.finish(context, player);
  }

  /** ADR 0004: an excluded container is never written to, under any configuration. */
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

  /** A container that has declared what it accepts has already said where those items belong. */
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

  /** #54: a full filtered container falls through rather than rejecting an item outright. */
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

  /** What fits is stored, what does not stays with the player, and the world keeps its total. */
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

  /**
   * A forged index is refused rather than clamped onto a real slot. Armour and the offhand are
   * named by their own inventory indices here, because those are the ones a client would reach for
   * if the range check were the only thing keeping them out.
   */
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

  /** Per ADR 0009 a request naming another screen reaches no network at all. */
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
