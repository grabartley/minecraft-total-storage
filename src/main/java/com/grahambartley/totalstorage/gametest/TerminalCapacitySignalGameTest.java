package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.block.TerminalBlock;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkCapacityService;
import com.grahambartley.totalstorage.terminal.TerminalCapacitySignal;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalCapacitySignalGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-capacity-signal";
  private static final int TICK_LIMIT = 60;
  private static final int THRESHOLD = 90;

  private final NetworkCapacityService capacities = new NetworkCapacityService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFullNetworkLightsTheBlockUp(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    context.assertTrue(show(context), "The block should have taken the near-capacity state");
    context.assertTrue(isWarning(context), "The block should read as near capacity");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anEmptyNetworkLeavesTheBlockAlone(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    context.assertTrue(!show(context), "An empty network should need no write");
    context.assertTrue(!isWarning(context), "An empty network should not warn");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theStateClearsWhenTheNetworkDropsBackBelowTheThreshold(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    show(context);

    emptyEast(context);
    show(context);

    context.assertTrue(!isWarning(context), "Emptying the network should clear the state");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void raisingTheThresholdPastAFullNetworkIsStillAWarning(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    context.assertTrue(
        TerminalCapacitySignal.warns(state(context), measured(context), 100),
        "A full network is at every threshold");
    context.assertTrue(
        !TerminalCapacitySignal.warns(state(context), measured(context), 0),
        "A threshold of nothing turns the warning off");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anUnpoweredTerminalNeverWarns(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkCapacity full = measured(context);

    BlockState unpowered = state(context).with(TerminalBlock.POWERED, false);

    context.assertTrue(
        !TerminalCapacitySignal.warns(unpowered, full, THRESHOLD),
        "An unpowered terminal shows no capacity state");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void writingTheStateItAlreadyHoldsIsSkipped(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    show(context);

    context.assertTrue(!show(context), "The second write should have been skipped");
    NetworkOperationFixture.finish(context, owner);
  }

  private static void emptyEast(TestContext context) {
    final BarrelBlockEntity barrel = context.getBlockEntity(NetworkOperationFixture.EAST_BARREL);
    barrel.clear();
    barrel.markDirty();
  }

  private boolean show(TestContext context) {
    return TerminalCapacitySignal.show(
        context.getWorld(),
        terminal(context),
        TerminalCapacitySignal.warns(state(context), measured(context), THRESHOLD));
  }

  private NetworkCapacity measured(TestContext context) {
    return capacities.measure(context.getWorld(), terminal(context));
  }

  private static BlockState state(TestContext context) {
    return context.getWorld().getBlockState(terminal(context));
  }

  private static boolean isWarning(TestContext context) {
    return state(context).get(TerminalBlock.NEAR_CAPACITY);
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }
}
