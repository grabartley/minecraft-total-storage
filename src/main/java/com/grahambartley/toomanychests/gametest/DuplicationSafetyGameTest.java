package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ConfigBuilder;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.lifecycle.ContainerLifecycle;
import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.operation.ConsolidationJob;
import com.grahambartley.toomanychests.operation.NetworkSortJob;
import com.grahambartley.toomanychests.operation.QuickStackService;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.operation.WithdrawOutcome;
import com.grahambartley.toomanychests.operation.WithdrawResult;
import com.grahambartley.toomanychests.operation.WithdrawService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.AfterBatch;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.ChunkPos;

public final class DuplicationSafetyGameTest implements FabricGameTest {
  private static final String BATCH = "duplication-safety";
  private static final int WITHIN_THE_STAGE = 1;

  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;
  private static final Item TRACKED = Items.DIAMOND;

  private final WithdrawService withdrawals = new WithdrawService();

  @BeforeBatch(batchId = BATCH)
  public void keepQuickStackInsideTheStage(ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ConfigBuilder.from(ServerConfigService.current())
            .quickStackRadius(WITHIN_THE_STAGE)
            .build());
  }

  @AfterBatch(batchId = BATCH)
  public void restoreQuickStackRadius(ServerWorld world) {
    ServerConfigService.update(
        world.getServer(),
        ConfigBuilder.from(ServerConfigService.current())
            .quickStackRadius(TooManyChestsConfig.DEFAULT_QUICK_STACK_RADIUS)
            .build());
  }

  private final QuickStackService quickStacks = new QuickStackService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void twoPlayersTakingTheLastStackTogetherConserveIt(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, TRACKED, STACK);
    ServerPlayerEntity first = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity second = NetworkOperationFixture.alsoOpen(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, first, second);

    WithdrawOutcome won = take(first, STACK, WithdrawDestination.CURSOR);
    WithdrawOutcome lost = take(second, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(
        won.moved() + lost.moved() == STACK,
        "The two withdrawals moved " + (won.moved() + lost.moved()) + " of one stack");
    NetworkOperationFixture.assertConserved(context, before, TRACKED, first, second);
    NetworkOperationFixture.finish(context, first, second);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalAgainstAContainerBrokenInTheSameTickConservesEverything(
      TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, TRACKED, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    context.setBlockState(NetworkOperationFixture.EAST_BARREL, Blocks.AIR);
    WithdrawOutcome outcome = take(player, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(
        outcome.moved() == 0L,
        "A container that has gone granted " + outcome.moved() + " items anyway");
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalAgainstAForgottenChunkConservesEverything(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, TRACKED, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    ContainerLifecycle.registry()
        .chunkUnloaded(
            context.getWorld().getRegistryKey(),
            new ChunkPos(context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL)));
    WithdrawOutcome outcome = take(player, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(
        outcome.result() == WithdrawResult.NOTHING_AVAILABLE,
        "An unloaded container should leave the network holding none of it, got "
            + outcome.result());
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void twoOverlappingTerminalsTakingFromOneBarrelConserveIt(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.SHARED_BARREL, TRACKED, STACK);
    ServerPlayerEntity south = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity north = NetworkOperationFixture.openNorthTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, south, north);

    WithdrawOutcome first = take(south, STACK, WithdrawDestination.CURSOR);
    WithdrawOutcome second = take(north, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(
        first.moved() + second.moved() == STACK,
        "Two terminals over one barrel moved "
            + (first.moved() + second.moved())
            + " of one stack");
    NetworkOperationFixture.assertConserved(context, before, TRACKED, south, north);
    NetworkOperationFixture.finish(context, south, north);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aForgedRequestForAnItemTheNetworkNeverHeldConjuresNothing(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, TRACKED, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, Items.NETHERITE_INGOT, player);

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
        "An item the network never held reported as " + outcome.result());
    NetworkOperationFixture.assertConserved(context, before, Items.NETHERITE_INGOT, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aForgedRequestForMoreThanTheNetworkHoldsGrantsOnlyWhatExists(TestContext context) {
    NetworkOperationFixture.stockedBarrel(context, NetworkOperationFixture.EAST_BARREL, TRACKED, 9);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    WithdrawOutcome outcome = take(player, STACK, WithdrawDestination.CURSOR);

    context.assertTrue(
        outcome.moved() == 9L, "Nine existed and the request moved " + outcome.moved());
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void clickingAStaleEntryRepeatedlyNeverAccumulatesItems(TestContext context) {
    NetworkOperationFixture.stockedBarrel(context, NetworkOperationFixture.EAST_BARREL, TRACKED, 5);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    for (int click = 0; click < 6; click++) {
      take(player, STACK, WithdrawDestination.INVENTORY);
    }

    context.assertTrue(
        NetworkOperationFixture.countCarried(player, TRACKED) == 5L,
        "Six clicks on five diamonds carried away "
            + NetworkOperationFixture.countCarried(player, TRACKED));
    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aNetworkSortAcrossTwoContainersConservesEverything(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, TRACKED, STACK);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, TRACKED, STACK / 2);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);

    NetworkOperationFixture.runToCompletion(
        context, new NetworkSortJob(NetworkOperationFixture.operationContext(context, player)));

    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aConsolidationAgainstAContainerBrokenMidwayConservesEverything(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, TRACKED, STACK / 4);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.WEST_BARREL, TRACKED, STACK / 4);
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.SHARED_BARREL, TRACKED, STACK / 4);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, player);
    ConsolidationJob job =
        new ConsolidationJob(NetworkOperationFixture.operationContext(context, player));

    job.advance(1);
    context.setBlockState(NetworkOperationFixture.SHARED_BARREL, Blocks.AIR);
    NetworkOperationFixture.runToCompletion(context, job);

    NetworkOperationFixture.assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void twoPlayersQuickStackingIntoOneBarrelConserveEverything(TestContext context) {
    NetworkOperationFixture.stockedBarrel(context, NetworkOperationFixture.EAST_BARREL, TRACKED, 1);
    ServerPlayerEntity first = NetworkOperationFixture.standingPlayer(context);
    ServerPlayerEntity second = NetworkOperationFixture.standingPlayer(context);
    first.getInventory().setStack(3, new ItemStack(TRACKED, STACK));
    second.getInventory().setStack(3, new ItemStack(TRACKED, STACK));
    long before = NetworkOperationFixture.countInWorld(context, TRACKED, first, second);

    quickStacks.quickStack(first);
    quickStacks.quickStack(second);

    NetworkOperationFixture.assertConserved(context, before, TRACKED, first, second);
    NetworkOperationFixture.finish(context, first, second);
  }

  private WithdrawOutcome take(
      ServerPlayerEntity player, int amount, WithdrawDestination destination) {
    return withdrawals.withdraw(
        player,
        new WithdrawRequestPayload(
            NetworkOperationFixture.handlerOf(player).syncId,
            ItemVariant.of(TRACKED),
            amount,
            destination));
  }
}
