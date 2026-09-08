package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.lifecycle.ContainerLifecycle;
import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.operation.WithdrawOutcome;
import com.grahambartley.toomanychests.operation.WithdrawResult;
import com.grahambartley.toomanychests.operation.WithdrawService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.ChunkPos;

/**
 * Adversarial proof that taking items out of a network can neither create nor destroy them.
 *
 * <p>This is the property that decides whether the mod is safe to put in a modpack, so it is proven
 * against the world rather than inferred from the pieces. Every test here counts every one of an
 * item in the test's patch of world before and after: in containers, carried by players, and lying
 * on the floor. A total that only counted containers could not tell an item that moved from one
 * that was made, and one that ignored the floor would score a duplication bug as a pass whenever
 * the extra copy happened to be dropped.
 *
 * <p>What is attacked here is the gap between what a client believes and what the network holds.
 * Per ADR 0009 that gap is permanent and ordinary, so each case widens it as far as it will go and
 * then clicks: the container is gone, its chunk has been forgotten, another player emptied it a
 * moment ago, or the request was never honest to begin with.
 *
 * <p>Any future change to a mutating code path adds a case here.
 */
public final class DuplicationSafetyGameTest implements FabricGameTest {
  private static final String BATCH = "duplication-safety";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;
  private static final Item TRACKED = Items.DIAMOND;

  private final WithdrawService withdrawals = new WithdrawService();

  /** Two players reaching for the last stack together: one stack in, one stack out. */
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
    assertConserved(context, before, TRACKED, first, second);
    NetworkOperationFixture.finish(context, first, second);
  }

  /**
   * A container broken in the same tick scatters its contents on the floor, and the withdrawal that
   * arrives afterwards must find nothing to take. The items are still in the world either way,
   * which is exactly what makes the total the assertion worth making.
   */
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
    assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * A chunk unload is published rather than performed, because the network's whole answer to one is
   * to forget the containers it named. The barrel keeps its items and the network stops offering
   * them, so a request against it must come back empty handed.
   */
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
    assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * Two terminals whose radii overlap on one barrel, both taking from it in the same tick. Each
   * holds its own index and neither knows about the other, so nothing but the transaction stands
   * between them and the same stack twice.
   */
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
    assertConserved(context, before, TRACKED, south, north);
    NetworkOperationFixture.finish(context, south, north);
  }

  /** A request naming an item no container in the network holds must conjure nothing. */
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
    assertConserved(context, before, Items.NETHERITE_INGOT, player);
    NetworkOperationFixture.finish(context, player);
  }

  /** A request for more than the network holds grants what exists and invents nothing. */
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
    assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
  }

  /** Clicking a listing that has gone stale over and over must never accumulate anything. */
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
    assertConserved(context, before, TRACKED, player);
    NetworkOperationFixture.finish(context, player);
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

  private static void assertConserved(
      TestContext context, long before, Item item, ServerPlayerEntity... players) {
    final long after = NetworkOperationFixture.countInWorld(context, item, players);
    context.assertTrue(
        after == before,
        "The world held " + before + " " + item.getName().getString() + " and now holds " + after);
  }
}
