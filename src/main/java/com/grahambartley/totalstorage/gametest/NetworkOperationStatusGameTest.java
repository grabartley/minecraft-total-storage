package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.network.NetworkOperationStatusPayload;
import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;
import com.grahambartley.totalstorage.operation.NetworkOperationStatus;
import com.grahambartley.totalstorage.screenhandler.TerminalViewerSync;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class NetworkOperationStatusGameTest implements FabricGameTest {
  private static final String BATCH = "network-operation-status";

  private static final int TICK_LIMIT = 40;

  private static TerminalAddress terminalOf(TestContext context) {
    return TerminalAddress.of(
        context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL));
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void everyPlayerOnTheTerminalIsToldAJobIsRunningOnTheirOwnScreen(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity first = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity second = NetworkOperationFixture.alsoOpen(context);

    List<ServerPlayerEntity> told =
        TerminalViewerSync.playersOn(context.getWorld().getServer(), terminalOf(context));

    context.assertTrue(
        told.contains(first) && told.contains(second),
        "Both players on the terminal should be told, told " + told.size());
    for (ServerPlayerEntity viewer : List.of(first, second)) {
      Optional<NetworkOperationStatusPayload> payload =
          NetworkOperationStatus.payloadFor(
              viewer, ExpensiveOperation.SORT_NETWORK, NetworkOperationPhase.RUNNING, 40);
      context.assertTrue(payload.isPresent(), "A player on the terminal got no status");
      context.assertEquals(
          payload.get().syncId(),
          NetworkOperationFixture.handlerOf(viewer).syncId,
          "The status should name the screen this player has open");
      context.assertEquals(
          payload.get().phase(), NetworkOperationPhase.RUNNING, "The status lost its phase");
      context.assertEquals(payload.get().percentDone(), 40, "The status lost its progress");
    }
    NetworkOperationFixture.finish(context, first, second);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoClosedTheTerminalIsToldNothing(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity player = NetworkOperationFixture.openTerminal(context);

    player.closeHandledScreen();

    context.assertTrue(
        NetworkOperationStatus.payloadFor(
                player, ExpensiveOperation.CONSOLIDATE, NetworkOperationPhase.IDLE, 0)
            .isEmpty(),
        "A player off the terminal was sent a status");
    context.assertFalse(
        TerminalViewerSync.playersOn(context.getWorld().getServer(), terminalOf(context))
            .contains(player),
        "A player off the terminal is still counted as watching it");
    NetworkOperationFixture.finish(context, player);
  }
}
