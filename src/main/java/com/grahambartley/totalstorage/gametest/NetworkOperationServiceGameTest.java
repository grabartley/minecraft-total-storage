package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.config.ConfigBuilder;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.operation.NetworkOperationService;
import com.grahambartley.totalstorage.operation.NetworkOperations;
import com.grahambartley.totalstorage.operation.OperationGuards;
import com.grahambartley.totalstorage.operation.OperationStart;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class NetworkOperationServiceGameTest implements FabricGameTest {
  private static final String BATCH = "network-operation-service";

  private static final int TICK_LIMIT = 60;
  private static final int SCATTERED = 20;

  private final NetworkOperationService operations = new NetworkOperationService();

  @BeforeBatch(batchId = BATCH)
  public void clearGuardsAndJobs(ServerWorld world) {
    OperationGuards.cooldowns().clear();
    NetworkOperations.jobs().clear();
    NetworkOperations.confirmations().clear();
    apply(
        world,
        ConfigBuilder.from(TotalStorageConfig.defaults()).expensiveOperationCooldownTicks(0));
  }

  @AfterBatch(batchId = BATCH)
  public void restoreConfig(ServerWorld world) {
    apply(world, ConfigBuilder.from(TotalStorageConfig.defaults()));
    NetworkOperations.jobs().clear();
    NetworkOperations.confirmations().clear();
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anOwnerStartsASortAndStopsItByAskingAgain(TestContext context) {
    scatteredNetwork(context);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    int syncId = NetworkOperationFixture.handlerOf(owner).syncId;

    OperationStart started = operations.sortNetwork(owner, syncId);
    OperationStart stopped = operations.sortNetwork(owner, syncId);

    context.assertTrue(
        started == OperationStart.STARTED, "The owner should start a sort, got " + started);
    context.assertTrue(
        stopped == OperationStart.CANCELLED, "Asking again should stop it, got " + stopped);
    context.assertFalse(
        NetworkOperations.jobs().isRunning(terminalAt(context)),
        "A cancelled operation should leave no job running");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aLargeNetworkAsksBeforeItStarts(TestContext context) {
    scatteredNetwork(context);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    apply(
        context.getWorld(),
        ConfigBuilder.from(ServerConfigService.current()).largeOperationContainers(1));
    int syncId = NetworkOperationFixture.handlerOf(owner).syncId;

    OperationStart asked = operations.sortNetwork(owner, syncId);
    OperationStart confirmed = operations.sortNetwork(owner, syncId);

    context.assertTrue(
        asked == OperationStart.NEEDS_CONFIRMATION,
        "A large network should ask first, got " + asked);
    context.assertTrue(
        confirmed == OperationStart.STARTED, "Asking again should start it, got " + confirmed);
    NetworkOperations.jobs().cancel(context.getWorld().getServer(), terminalAt(context), owner);
    apply(
        context.getWorld(),
        ConfigBuilder.from(ServerConfigService.current())
            .largeOperationContainers(TotalStorageConfig.DEFAULT_LARGE_OPERATION_CONTAINERS));
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRateLimitedRequestNeverReachesTheNetwork(TestContext context) {
    scatteredNetwork(context);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    apply(
        context.getWorld(),
        ConfigBuilder.from(ServerConfigService.current()).expensiveOperationCooldownTicks(200));
    int syncId = NetworkOperationFixture.handlerOf(owner).syncId;
    operations.consolidate(owner, syncId);
    NetworkOperations.jobs().cancel(context.getWorld().getServer(), terminalAt(context), owner);

    OperationStart tooSoon = operations.consolidate(owner, syncId);

    context.assertTrue(
        tooSoon == OperationStart.RATE_LIMITED,
        "A second consolidation should be paced, got " + tooSoon);
    context.assertFalse(
        NetworkOperations.jobs().isRunning(terminalAt(context)),
        "A rate limited request must not start a job");
    apply(
        context.getWorld(),
        ConfigBuilder.from(ServerConfigService.current()).expensiveOperationCooldownTicks(0));
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNetworkWithNothingScatteredIsNotWorthConsolidating(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, SCATTERED);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    OperationStart nothing =
        operations.consolidate(owner, NetworkOperationFixture.handlerOf(owner).syncId);

    context.assertTrue(
        nothing == OperationStart.NOTHING_TO_DO,
        "One barrel holds nothing scattered, got " + nothing);
    context.assertFalse(
        NetworkOperations.jobs().isRunning(terminalAt(context)),
        "Nothing to do should leave no job running");
    NetworkOperationFixture.finish(context, owner);
  }

  private static void scatteredNetwork(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.COBBLESTONE, SCATTERED);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, Items.COBBLESTONE, SCATTERED);
  }

  private static TerminalAddress terminalAt(TestContext context) {
    return TerminalAddress.of(
        context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL));
  }

  private static void apply(ServerWorld world, ConfigBuilder config) {
    ServerConfigService.update(world.getServer(), config.build());
  }
}
