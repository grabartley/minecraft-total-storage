package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.operation.NetworkMutation;
import com.grahambartley.toomanychests.operation.PlayerSlots;
import com.grahambartley.toomanychests.operation.TerminalOperationContext;
import com.grahambartley.toomanychests.operation.TerminalOperationGate;
import com.grahambartley.toomanychests.operation.WithdrawSources;
import com.grahambartley.toomanychests.transaction.OperationResult;
import com.grahambartley.toomanychests.transaction.StorageTransfers;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * The contract every mutating path in the mod runs through: watch, run, tell, and always stop
 * watching.
 *
 * <p>Only the last of those can fail quietly. A witness left registered keeps hearing about
 * containers for the rest of the server's life and keeps the operation that opened it alive with
 * them, and nothing about the operation's own result would say so. Both endings are checked for it,
 * including the one the runner produces on its own when an operation throws.
 */
public final class NetworkMutationGameTest implements FabricGameTest {
  private static final String BATCH = "network-mutation";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  /** A committed operation still leaves the registry exactly as it found it. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anOperationThatCommittedLeavesNothingListeningBehindIt(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    int watchedBefore = ContainerMutations.registry().watchedContainerCount();

    TerminalOperationContext operating = contextOf(player);
    OperationResult<Long> result =
        NetworkMutation.run(
            operating,
            transaction ->
                OperationResult.committed(
                    StorageTransfers.gather(
                        new WithdrawSources()
                            .holding(
                                operating.world(),
                                operating.network(),
                                ItemVariant.of(Items.DIAMOND)),
                        PlayerSlots.unlockedStorage(player),
                        ItemVariant.of(Items.DIAMOND),
                        STACK,
                        transaction)));

    context.assertTrue(result.committed(), "The operation should have committed");
    assertNothingLeftListening(context, watchedBefore);
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * An operation that threw is rolled back by the runner rather than propagating, so the fan-out
   * still runs afterwards and the witness still has to come off the registry.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anOperationThatThrewLeavesNothingListeningBehindIt(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    int watchedBefore = ContainerMutations.registry().watchedContainerCount();

    OperationResult<Long> result =
        NetworkMutation.run(
            contextOf(player),
            transaction -> {
              throw new IllegalStateException("a network operation that could not finish");
            });

    context.assertTrue(result.rolledBack(), "An operation that threw should have rolled back");
    assertNothingLeftListening(context, watchedBefore);
    NetworkOperationFixture.finish(context, player);
  }

  /** An operation that rolled back on its own terms leaves the registry alone as well. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anOperationThatRolledBackLeavesNothingListeningBehindIt(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    int watchedBefore = ContainerMutations.registry().watchedContainerCount();

    OperationResult<Long> result =
        NetworkMutation.run(
            contextOf(player), transaction -> OperationResult.rolledBack("nothing worth keeping"));

    context.assertTrue(result.rolledBack(), "The operation should have rolled back");
    assertNothingLeftListening(context, watchedBefore);
    NetworkOperationFixture.finish(context, player);
  }

  private static void assertNothingLeftListening(TestContext context, int watchedBefore) {
    context.assertTrue(
        ContainerMutations.registry().watchedContainerCount() == watchedBefore,
        "A witness outlived the operation that opened it, the registry went from "
            + watchedBefore
            + " to "
            + ContainerMutations.registry().watchedContainerCount());
    context.assertTrue(
        ContainerMutations.registry().watchedEverywhereCount() == 0,
        "A listener was left hearing about every container on the server");
  }

  private static TerminalOperationContext contextOf(ServerPlayerEntity player) {
    return TerminalOperationGate.open(player, NetworkOperationFixture.handlerOf(player).syncId)
        .orElseThrow();
  }
}
