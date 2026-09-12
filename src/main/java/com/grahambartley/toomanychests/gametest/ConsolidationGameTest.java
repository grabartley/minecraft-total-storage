package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.operation.ConsolidationJob;
import com.grahambartley.toomanychests.operation.NetworkJob;
import com.grahambartley.toomanychests.operation.NetworkJobState;
import com.grahambartley.toomanychests.operation.NetworkOperationContext;
import com.grahambartley.toomanychests.operation.NetworkOperations;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.operation.OperationReport;
import com.grahambartley.toomanychests.storage.ContainerAssignment;
import com.grahambartley.toomanychests.storage.ContainerFilter;
import com.grahambartley.toomanychests.storage.ContainerRole;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class ConsolidationGameTest implements FabricGameTest {
  private static final String BATCH = "network-consolidate";

  private static final int TICK_LIMIT = 80;
  private static final int BUDGET = 64;
  private static final int ONE_STEP = 1;
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
  public void oneVariantSpreadAcrossThreeBarrelsIsGatheredIntoTheFewest(TestContext context) {
    BarrelBlockEntity east =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 20);
    BarrelBlockEntity west =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, 30);
    BarrelBlockEntity shared =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.SHARED_BARREL, Items.COBBLESTONE, 14);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, Items.COBBLESTONE, owner);

    OperationReport report = runToCompletion(new ConsolidationJob(contextFor(context, owner)));

    context.assertEquals(
        barrelsHolding(east, west, shared), 1, "Cobblestone should end up in one barrel");
    context.assertTrue(report.itemsMoved() > 0L, "Consolidation should report what it gathered");
    NetworkOperationFixture.assertConserved(context, before, Items.COBBLESTONE, owner);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedBarrelIsNeitherReadFromNorWrittenTo(TestContext context) {
    BarrelBlockEntity excluded =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 20);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, 30);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.SHARED_BARREL, Items.COBBLESTONE, 14);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context, NetworkOperationFixture.EAST_BARREL, ContainerRole.EXCLUDED);
    long before = NetworkOperationFixture.countInWorld(context, Items.COBBLESTONE, owner);

    runToCompletion(new ConsolidationJob(contextFor(context, owner)));

    context.assertEquals(
        (int) NetworkOperationFixture.countIn(excluded, Items.COBBLESTONE),
        20,
        "An excluded barrel must hold exactly what it held");
    context.assertTrue(
        excluded.getStack(0).getCount() == 20,
        "An excluded barrel's slots must be untouched, found " + excluded.getStack(0));
    NetworkOperationFixture.assertConserved(context, before, Items.COBBLESTONE, owner);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFilteredBarrelOnlyEverReceivesWhatItsFilterNames(TestContext context) {
    BarrelBlockEntity filtered =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 4);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, 30);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.SHARED_BARREL, Items.COBBLESTONE, 14);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkOperationFixture.assign(
        context,
        NetworkOperationFixture.EAST_BARREL,
        new ContainerAssignment(
            ContainerRole.FILTERED, ContainerFilter.ofItems("minecraft:diamond")));

    runToCompletion(new ConsolidationJob(contextFor(context, owner)));

    context.assertEquals(
        (int) NetworkOperationFixture.countIn(filtered, Items.COBBLESTONE),
        0,
        "A filtered barrel must never receive an item it does not accept");
    context.assertEquals(
        (int) NetworkOperationFixture.countIn(filtered, Items.DIAMOND),
        4,
        "A filtered barrel keeps what it does accept");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNetworkTooBigForOneTickIsGatheredAcrossSeveral(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 20);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, 30);
    BarrelBlockEntity shared =
        NetworkOperationFixture.stockedBarrel(
            context, NetworkOperationFixture.SHARED_BARREL, Items.DIAMOND, 6);
    shared.setStack(1, new ItemStack(Items.COBBLESTONE, 14));
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, Items.COBBLESTONE, owner);
    ConsolidationJob job = new ConsolidationJob(contextFor(context, owner));

    NetworkJobState afterOneStep = job.advance(ONE_STEP);

    context.assertTrue(
        afterOneStep.isRunning(), "A budget of one step should not finish the whole network");
    runToCompletion(job);
    context.assertTrue(job.report().percentDone() == 100, "The job should finish across ticks");
    NetworkOperationFixture.assertConserved(context, before, Items.COBBLESTONE, owner);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void cancellingPartWayThroughLosesNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 20);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, 30);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.SHARED_BARREL, Items.COBBLESTONE, 14);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, Items.COBBLESTONE, owner);
    ConsolidationJob job = new ConsolidationJob(contextFor(context, owner));

    job.advance(ONE_STEP);
    job.cancel();

    context.assertTrue(job.report().cancelled(), "A cancelled job should say so");
    context.assertTrue(
        job.advance(BUDGET) == NetworkJobState.DONE, "A cancelled job should do no more work");
    NetworkOperationFixture.assertConserved(context, before, Items.COBBLESTONE, owner);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aBarrelBrokenPartWayThroughLosesNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, 20);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, 30);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.SHARED_BARREL, Items.COBBLESTONE, 14);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, Items.COBBLESTONE, owner);
    ConsolidationJob job = new ConsolidationJob(contextFor(context, owner));

    job.advance(ONE_STEP);
    context.setBlockState(NetworkOperationFixture.WEST_BARREL, Blocks.AIR.getDefaultState());
    runToCompletion(job);

    NetworkOperationFixture.assertConserved(context, before, Items.COBBLESTONE, owner);
    NetworkOperationFixture.finish(context, owner);
  }

  private static int barrelsHolding(BarrelBlockEntity... barrels) {
    int holding = 0;
    for (final BarrelBlockEntity barrel : barrels) {
      if (NetworkOperationFixture.countIn(barrel, Items.COBBLESTONE) > 0L) {
        holding++;
      }
    }
    return holding;
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
