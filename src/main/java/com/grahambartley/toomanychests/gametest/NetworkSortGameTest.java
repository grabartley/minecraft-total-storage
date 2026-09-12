package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.network.TerminalActionPayload;
import com.grahambartley.toomanychests.operation.NetworkJob;
import com.grahambartley.toomanychests.operation.NetworkOperationContext;
import com.grahambartley.toomanychests.operation.NetworkOperations;
import com.grahambartley.toomanychests.operation.NetworkSortJob;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.OperationReport;
import com.grahambartley.toomanychests.screenhandler.TerminalActions;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsEdits;
import com.grahambartley.toomanychests.screenhandler.TerminalSettingsService;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.TerminalAction;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class NetworkSortGameTest implements FabricGameTest {
  private static final String BATCH = "network-sort";

  private static final int TICK_LIMIT = 60;
  private static final int BUDGET = 64;
  private static final int MOST_STEPS_A_TEST_NEEDS = 64;

  @BeforeBatch(batchId = BATCH)
  public void clearGuardsAndJobs(ServerWorld world) {
    OperationGuards.cooldowns().clear();
    NetworkOperations.jobs().clear();
    NetworkOperations.confirmations().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNetworkSortCompactsEveryContainerItHolds(TestContext context) {
    BarrelBlockEntity east =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    BarrelBlockEntity west =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    scatter(east, Items.COBBLESTONE, 20, 30);
    scatter(west, Items.DIAMOND, 5, 6);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, Items.COBBLESTONE, owner);

    OperationReport report = runToCompletion(new NetworkSortJob(contextFor(context, owner)));

    context.assertTrue(
        report.containersSorted() >= 2,
        "Both barrels should have been sorted, sorted " + report.containersSorted());
    context.assertEquals(east.getStack(0).getCount(), 50, "The cobblestone should be one stack");
    context.assertEquals(west.getStack(0).getCount(), 11, "The diamonds should be one stack");
    NetworkOperationFixture.assertConserved(context, before, Items.COBBLESTONE, owner);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void sortingAnAlreadySortedNetworkMovesNothing(TestContext context) {
    BarrelBlockEntity east =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    scatter(east, Items.COBBLESTONE, 20, 30);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    runToCompletion(new NetworkSortJob(contextFor(context, owner)));

    OperationReport second = runToCompletion(new NetworkSortJob(contextFor(context, owner)));

    context.assertEquals(
        second.containersSorted(), 0, "A sorted network should need no second pass");
    context.assertEquals(east.getStack(0).getCount(), 50, "The arrangement should be unchanged");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedContainerIsLeftExactlyAsItWasAndReportedAsSkipped(TestContext context) {
    BarrelBlockEntity excluded =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    scatter(excluded, Items.COBBLESTONE, 20, 30);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context, NetworkOperationFixture.EAST_BARREL, ContainerRole.EXCLUDED);

    OperationReport report = runToCompletion(new NetworkSortJob(contextFor(context, owner)));

    context.assertEquals(
        excluded.getStack(0).getCount(), 20, "An excluded container must not be rearranged");
    context.assertEquals(
        excluded.getStack(4).getCount(), 30, "An excluded container must not be rearranged");
    context.assertTrue(
        report.excludedSkipped() >= 1,
        "The summary should say an excluded container was skipped, said "
            + report.excludedSkipped());
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerWithSlotsThisModWillNotRearrangeIsSkippedRatherThanRisked(
      TestContext context) {
    context.setBlockState(NetworkOperationFixture.EAST_BARREL, Blocks.FURNACE.getDefaultState());
    Inventory furnace = context.getBlockEntity(NetworkOperationFixture.EAST_BARREL);
    furnace.setStack(0, new ItemStack(Items.COBBLESTONE, 8));
    furnace.setStack(1, new ItemStack(Items.COAL, 4));
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    runToCompletion(new NetworkSortJob(contextFor(context, owner)));

    context.assertTrue(
        furnace.getStack(0).isOf(Items.COBBLESTONE) && furnace.getStack(0).getCount() == 8,
        "A furnace's input slot must be left where it was, found " + furnace.getStack(0));
    context.assertTrue(
        furnace.getStack(1).isOf(Items.COAL), "A furnace's fuel must stay in the fuel slot");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWatchingAContainerSeesTheNetworkSortThroughTheirOwnScreen(
      TestContext context) {
    BarrelBlockEntity east =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    scatter(east, Items.COBBLESTONE, 20, 30);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity watcher = watching(context, NetworkOperationFixture.EAST_BARREL);

    runToCompletion(new NetworkSortJob(contextFor(context, owner)));

    ItemStack throughWatcher = watcher.currentScreenHandler.getSlot(0).getStack();
    context.assertTrue(
        throughWatcher.isOf(Items.COBBLESTONE) && throughWatcher.getCount() == 50,
        "The watcher's screen should show the merged stack, found " + throughWatcher);
    NetworkOperationFixture.finish(context, owner, watcher);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoOnlyMayUseTheTerminalCannotSortTheNetwork(TestContext context) {
    BarrelBlockEntity east =
        NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    scatter(east, Items.COBBLESTONE, 20, 30);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity guest = NetworkOperationFixture.alsoOpen(context);
    TerminalActions actions = new TerminalActions(new TerminalSettingsEdits(panels()));

    boolean acted =
        actions.act(
            guest,
            new TerminalActionPayload(
                NetworkOperationFixture.handlerOf(guest).syncId, TerminalAction.SORT_NETWORK));

    context.assertFalse(acted, "A guest must not be able to restructure someone else's storage");
    context.assertEquals(
        east.getStack(0).getCount(), 20, "A refused request must not move anything");
    context.assertFalse(
        NetworkOperations.jobs()
            .isRunning(
                com.grahambartley.toomanychests.terminal.TerminalAddress.of(
                    context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL))),
        "A refused request must not leave a job running");
    NetworkOperationFixture.finish(context, owner, guest);
  }

  private static TerminalSettingsService panels() {
    return new TerminalSettingsService();
  }

  private static ServerPlayerEntity watching(TestContext context, BlockPos container) {
    ServerPlayerEntity watcher = NetworkOperationFixture.standingPlayer(context);
    context.useBlock(container, watcher);
    return watcher;
  }

  private static void scatter(
      Inventory container, net.minecraft.item.Item item, int first, int second) {
    container.setStack(0, new ItemStack(item, first));
    container.setStack(4, new ItemStack(item, second));
    container.markDirty();
  }

  private static NetworkOperationContext contextFor(
      TestContext context, ServerPlayerEntity player) {
    return new NetworkOperationContext(
        context.getWorld(),
        context.getAbsolutePos(NetworkOperationFixture.TERMINAL),
        player.getUuid());
  }

  private static OperationReport runToCompletion(NetworkJob job) {
    int steps = 0;
    while (job.advance(BUDGET).isRunning() && steps < MOST_STEPS_A_TEST_NEEDS) {
      steps++;
    }
    return job.report();
  }
}
