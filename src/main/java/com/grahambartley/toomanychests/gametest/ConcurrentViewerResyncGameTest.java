package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.network.WithdrawRequestPayload;
import com.grahambartley.toomanychests.operation.NetworkMutation;
import com.grahambartley.toomanychests.operation.NetworkResync;
import com.grahambartley.toomanychests.operation.TerminalOperationContext;
import com.grahambartley.toomanychests.operation.TerminalOperationGate;
import com.grahambartley.toomanychests.operation.WithdrawDestination;
import com.grahambartley.toomanychests.operation.WithdrawService;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.storage.BlockStorageOrigin;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

/**
 * Whether everybody looking at a network hears about a move, and only the people who should.
 *
 * <p>A commit is the moment a listing on somebody else's screen becomes wrong, and it is the moment
 * this proves they are told. Every assertion here is made in the same tick as the withdrawal that
 * caused it, because a resync that waits for the next tick sweep leaves a window in which the
 * bystander's next click is a request the server has to refuse.
 *
 * <p>A page reaching a client cannot be seen from a gametest, because a mock player never declares
 * the mod's channels and the send goes nowhere. Every page built is stamped with a new revision
 * though, and the stamp advancing is the same evidence a packet would have been.
 *
 * <p>The other audience, a player with a container open by hand, is counted rather than stamped: a
 * vanilla screen carries no revision of its own, so what is asserted is how many screens the
 * fan-out reached.
 */
public final class ConcurrentViewerResyncGameTest implements FabricGameTest {
  private static final String BATCH = "concurrent-viewer-resync";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  private final WithdrawService withdrawals = new WithdrawService();

  /** The case the standards call the test case: two players doing one thing at one object. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalResyncsTheOtherPlayerAtTheSameTerminal(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity acting =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));
    ServerPlayerEntity bystander =
        NetworkOperationFixture.watching(NetworkOperationFixture.alsoOpen(context));
    long before = revisionOf(bystander);

    take(acting, STACK);

    context.assertTrue(
        revisionOf(bystander) > before,
        "The other player at the terminal was left looking at a stale listing");
    NetworkOperationFixture.finish(context, acting, bystander);
  }

  /** Two radii over one barrel: a move through either is a move the other is wrong about. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalResyncsAViewerOfAnOverlappingTerminal(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.SHARED_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity south =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));
    ServerPlayerEntity north =
        NetworkOperationFixture.watching(NetworkOperationFixture.openNorthTerminal(context));
    long before = revisionOf(north);

    take(south, STACK);

    context.assertTrue(
        revisionOf(north) > before,
        "The overlapping terminal was left offering a barrel that had just been emptied");
    NetworkOperationFixture.finish(context, south, north);
  }

  /** A terminal whose network holds none of what moved hears nothing at all. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aWithdrawalLeavesAloneAViewerWhoCannotSeeTheContainer(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity south =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));
    ServerPlayerEntity north =
        NetworkOperationFixture.watching(NetworkOperationFixture.openNorthTerminal(context));
    long before = revisionOf(north);

    take(south, STACK);

    context.assertTrue(
        revisionOf(north) == before,
        "A terminal holding none of what moved was resynced anyway, stamp went to "
            + revisionOf(north));
    NetworkOperationFixture.finish(context, south, north);
  }

  /** The player who acted is answered by their own operation, not a second time by the fan-out. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theActingPlayerIsNotResyncedTwice(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity acting =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));

    int reached =
        NetworkResync.after(
            context.getWorld().getServer(),
            List.of(originOf(context, NetworkOperationFixture.EAST_BARREL)),
            acting.getUuid());

    context.assertTrue(
        reached == 0,
        "The acting player's own screen was resent by the fan-out as well, reaching " + reached);
    NetworkOperationFixture.finish(context, acting);
  }

  /** A chest somebody opened by hand is a screen the terminal has just written behind. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerOpenByHandIsResyncedWhenTheNetworkMovesItsContents(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity acting = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity byHand = NetworkOperationFixture.standingPlayer(context);
    context.useBlock(NetworkOperationFixture.EAST_BARREL, byHand);

    int reached =
        NetworkResync.after(
            context.getWorld().getServer(),
            List.of(originOf(context, NetworkOperationFixture.EAST_BARREL)),
            acting.getUuid());

    context.assertTrue(
        reached == 1,
        "The player with the barrel open by hand should have been the one screen resent, reached "
            + reached);
    NetworkOperationFixture.finish(context, acting, byHand);
  }

  /** A screen open on something else is none of the fan-out's business. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aContainerOpenByHandElsewhereIsLeftAlone(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity acting = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity byHand = NetworkOperationFixture.standingPlayer(context);
    context.useBlock(NetworkOperationFixture.WEST_BARREL, byHand);

    int reached =
        NetworkResync.after(
            context.getWorld().getServer(),
            List.of(originOf(context, NetworkOperationFixture.EAST_BARREL)),
            acting.getUuid());

    context.assertTrue(
        reached == 0, "A screen open on another container was resent anyway, reaching " + reached);
    NetworkOperationFixture.finish(context, acting, byHand);
  }

  /** Every viewer leaving by the ordinary route empties the register they were written into. */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theViewerRegisterIsEmptyOnceEverybodyHasClosedTheirScreen(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity first = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity second = NetworkOperationFixture.alsoOpen(context);
    TerminalAddress terminal =
        TerminalAddress.of(
            context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL));
    context.assertTrue(
        TerminalAccessControl.viewers().viewing(terminal).size() == 2,
        "Both players should have been written into the viewer register");

    first.closeHandledScreen();
    second.closeHandledScreen();

    context.assertTrue(
        TerminalAccessControl.viewers().viewing(terminal).isEmpty(),
        "The viewer register still names "
            + TerminalAccessControl.viewers().viewing(terminal).size()
            + " players who have closed their screens");
    NetworkOperationFixture.finish(context, first, second);
  }

  /**
   * A viewer who left without their screen saying so, which is what a disconnect looks like from
   * here. The register heals on the next walk of it rather than holding the entry for ever.
   */
  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aViewerWhoLeftWithoutClosingIsDroppedFromTheRegister(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, STACK);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);
    TerminalAddress terminal =
        TerminalAddress.of(
            context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL));
    TerminalAccessControl.viewers().opened(terminal, UUID.randomUUID());

    NetworkResync.after(
        context.getWorld().getServer(),
        List.of(originOf(context, NetworkOperationFixture.EAST_BARREL)),
        player.getUuid());

    context.assertTrue(
        TerminalAccessControl.viewers().viewing(terminal).size() == 1,
        "A viewer with no player behind them should have been dropped, register holds "
            + TerminalAccessControl.viewers().viewing(terminal).size());
    NetworkOperationFixture.finish(context, player);
  }

  /**
   * An operation that threw is rolled back by the runner rather than propagating, so the fan-out
   * still runs and the witness still has to come off the registry. One left behind would hear about
   * every container it was watching for the rest of the server's life.
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

    NetworkMutation.run(
        contextOf(player),
        transaction -> {
          throw new IllegalStateException("a network operation that could not finish");
        });

    context.assertTrue(
        ContainerMutations.registry().watchedContainerCount() == watchedBefore,
        "A witness outlived the operation that opened it, registry went from "
            + watchedBefore
            + " to "
            + ContainerMutations.registry().watchedContainerCount());
    NetworkOperationFixture.finish(context, player);
  }

  private static TerminalOperationContext contextOf(ServerPlayerEntity player) {
    return TerminalOperationGate.open(player, NetworkOperationFixture.handlerOf(player).syncId)
        .orElseThrow();
  }

  private void take(ServerPlayerEntity player, int amount) {
    withdrawals.withdraw(
        player,
        new WithdrawRequestPayload(
            NetworkOperationFixture.handlerOf(player).syncId,
            ItemVariant.of(Items.DIAMOND),
            amount,
            WithdrawDestination.CURSOR));
  }

  private static long revisionOf(ServerPlayerEntity player) {
    return ((TerminalScreenHandler) player.currentScreenHandler).listing().revision();
  }

  private static StorageOrigin originOf(TestContext context, BlockPos where) {
    return BlockStorageOrigin.of(context.getWorld(), context.getAbsolutePos(where));
  }
}
