package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.QuickStackOutcome;
import com.grahambartley.toomanychests.operation.QuickStackService;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerFilter;
import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.List;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class QuickStackServiceGameTest implements FabricGameTest {
  private static final String BATCH = "quick-stack";

  private static final int TICK_LIMIT = 60;

  private final QuickStackService quickStacks = new QuickStackService();

  @BeforeBatch(batchId = BATCH)
  public void clearCooldowns(ServerWorld world) {
    OperationGuards.cooldowns().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void quickStackTopsUpABarrelThatAlreadyHoldsTheItem(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 10);
    ServerPlayerEntity player = NetworkOperationFixture.standingPlayer(context);
    player.getInventory().setStack(3, new ItemStack(Items.COBBLESTONE, 20));
    long before = NetworkOperationFixture.countInWorld(context, Items.COBBLESTONE, player);

    QuickStackOutcome outcome = quickStacks.quickStack(player);

    context.assertTrue(outcome.result().movedAnything(), "Quick stack should have moved something");
    context.assertEquals(
        (int) NetworkOperationFixture.countIn(barrel, Items.COBBLESTONE),
        30,
        "The barrel should have been topped up");
    context.assertTrue(
        player.getInventory().getStack(3).isEmpty(), "The player's stack should have gone");
    NetworkOperationFixture.assertConserved(context, before, Items.COBBLESTONE, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void quickStackNeverIntroducesAnItemAContainerDoesNotAlreadyHold(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 10);
    ServerPlayerEntity player = NetworkOperationFixture.standingPlayer(context);
    player.getInventory().setStack(3, new ItemStack(Items.DIAMOND, 5));

    QuickStackOutcome outcome = quickStacks.quickStack(player);

    context.assertFalse(
        outcome.result().movedAnything(), "There was nothing the barrel already held");
    context.assertEquals(
        (int) NetworkOperationFixture.countIn(barrel, Items.DIAMOND),
        0,
        "Quick stack must not introduce a new item");
    context.assertEquals(
        player.getInventory().getStack(3).getCount(), 5, "The diamonds should stay carried");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aLockedSlotIsNeverQuickStacked(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 10);
    ServerPlayerEntity player = NetworkOperationFixture.standingPlayer(context);
    LockedSlotsAttachment.set(player, LockedSlots.of(List.of(3)));
    player.getInventory().setStack(3, new ItemStack(Items.COBBLESTONE, 20));

    quickStacks.quickStack(player);

    context.assertEquals(
        player.getInventory().getStack(3).getCount(),
        20,
        "A locked slot must be left exactly as it was");
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedContainerIsSkippedWhenATerminalClaimsIt(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 10);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context, NetworkOperationFixture.EAST_BARREL, ContainerRole.EXCLUDED);
    owner.getInventory().setStack(3, new ItemStack(Items.COBBLESTONE, 20));

    quickStacks.quickStack(owner);

    context.assertEquals(
        (int) NetworkOperationFixture.countIn(barrel, Items.COBBLESTONE),
        10,
        "An excluded container must not be quick stacked into");
    context.assertEquals(
        owner.getInventory().getStack(3).getCount(), 20, "The items should stay carried");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFilteredContainerOnlyReceivesWhatItAccepts(TestContext context) {
    BarrelBlockEntity barrel =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 10);
    barrel.setStack(1, new ItemStack(Items.DIAMOND, 2));
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context,
        NetworkOperationFixture.EAST_BARREL,
        new ContainerAssignment(
            ContainerRole.FILTERED, ContainerFilter.ofItems("minecraft:diamond")));
    owner.getInventory().setStack(3, new ItemStack(Items.COBBLESTONE, 20));
    owner.getInventory().setStack(4, new ItemStack(Items.DIAMOND, 3));

    quickStacks.quickStack(owner);

    context.assertEquals(
        (int) NetworkOperationFixture.countIn(barrel, Items.COBBLESTONE),
        10,
        "A filtered container must refuse what it does not accept");
    context.assertEquals(
        (int) NetworkOperationFixture.countIn(barrel, Items.DIAMOND),
        5,
        "A filtered container should take what it does accept");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void quickStackReportsWhenNothingNearbyWantsAnything(TestContext context) {
    ServerPlayerEntity player = NetworkOperationFixture.standingPlayer(context);
    player.getInventory().setStack(3, new ItemStack(Items.COBBLESTONE, 20));

    QuickStackOutcome outcome = quickStacks.quickStack(player);

    context.assertFalse(outcome.result().movedAnything(), "There is nothing nearby to stack into");
    context.assertEquals(
        player.getInventory().getStack(3).getCount(), 20, "The items should stay carried");
    NetworkOperationFixture.finish(context, player);
  }
}
