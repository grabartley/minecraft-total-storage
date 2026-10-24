package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.access.TerminalAccessControl;
import com.grahambartley.totalstorage.network.WithdrawRequestPayload;
import com.grahambartley.totalstorage.operation.NetworkResync;
import com.grahambartley.totalstorage.operation.WithdrawDestination;
import com.grahambartley.totalstorage.operation.WithdrawService;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ConcurrentViewerResyncGameTest implements FabricGameTest {
  private static final String BATCH = "concurrent-viewer-resync";
  private static final int TICK_LIMIT = 60;
  private static final int STACK = 64;

  private final WithdrawService withdrawals = new WithdrawService();

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
