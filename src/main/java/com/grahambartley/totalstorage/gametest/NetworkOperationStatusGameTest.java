package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;
import com.grahambartley.totalstorage.operation.NetworkOperationStatus;
import com.grahambartley.totalstorage.operation.NetworkOperationStatus.Delivery;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

  private static void assertEachViewerGets(
      TestContext context,
      List<Delivery> deliveries,
      Set<ServerPlayerEntity> expected,
      NetworkOperationPhase phase,
      int percent) {
    context.assertEquals(
        deliveries.stream().map(Delivery::player).collect(Collectors.toSet()),
        expected,
        "The status went to the wrong players");
    for (Delivery delivery : deliveries) {
      context.assertEquals(
          delivery.payload().syncId(),
          NetworkOperationFixture.handlerOf(delivery.player()).syncId,
          "The status should name the screen this player has open");
      context.assertEquals(delivery.payload().phase(), phase, "The status carried the wrong phase");
      context.assertEquals(
          delivery.payload().percentDone(), percent, "The status carried the wrong progress");
    }
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aRunningJobIsShownToEveryPlayerAtTheTerminal(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity first = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity second = NetworkOperationFixture.alsoOpen(context);

    assertEachViewerGets(
        context,
        NetworkOperationStatus.runningDeliveries(
            context.getWorld().getServer(),
            terminalOf(context),
            ExpensiveOperation.SORT_NETWORK,
            40),
        Set.of(first, second),
        NetworkOperationPhase.RUNNING,
        40);
    NetworkOperationFixture.finish(context, first, second);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFinishedJobClearsTheButtonForEveryPlayerAtTheTerminal(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity first = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity second = NetworkOperationFixture.alsoOpen(context);

    assertEachViewerGets(
        context,
        NetworkOperationStatus.idleDeliveries(
            context.getWorld().getServer(), terminalOf(context), ExpensiveOperation.CONSOLIDATE),
        Set.of(first, second),
        NetworkOperationPhase.IDLE,
        0);
    NetworkOperationFixture.finish(context, first, second);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anArmedButtonIsShownOnlyToThePlayerWhoPressedIt(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity presser = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity other = NetworkOperationFixture.alsoOpen(context);

    assertEachViewerGets(
        context,
        NetworkOperationStatus.armedDeliveries(presser, ExpensiveOperation.SORT_NETWORK),
        Set.of(presser),
        NetworkOperationPhase.ARMED,
        0);
    NetworkOperationFixture.finish(context, presser, other);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aPlayerWhoClosedTheTerminalIsToldNothing(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity left = NetworkOperationFixture.openTerminal(context);
    ServerPlayerEntity stayed = NetworkOperationFixture.alsoOpen(context);

    left.closeHandledScreen();

    assertEachViewerGets(
        context,
        NetworkOperationStatus.runningDeliveries(
            context.getWorld().getServer(),
            terminalOf(context),
            ExpensiveOperation.SORT_NETWORK,
            10),
        Set.of(stayed),
        NetworkOperationPhase.RUNNING,
        10);
    context.assertTrue(
        NetworkOperationStatus.armedDeliveries(left, ExpensiveOperation.SORT_NETWORK).isEmpty(),
        "A player off the terminal was sent a status");
    NetworkOperationFixture.finish(context, left, stayed);
  }
}
