package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.block.TerminalBlock;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.terminal.NetworkCapacityService;
import com.grahambartley.toomanychests.terminal.NetworkCapacityWatch;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalCapacityBroadcast;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class TerminalCapacityBroadcastGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-capacity-broadcast";
  private static final int TICK_LIMIT = 60;

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFillingNetworkLightsItsOwnBlockUpWithNoScreenOpen(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    owner.closeHandledScreen();

    sweep(context);

    context.assertTrue(isWarning(context), "The sweep should have lit the block on its own");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theSweepTakesTheStateBackOffWhenTheNetworkEmpties(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    TerminalCapacityBroadcast sweeping = sweeper();
    sweeping.sweep(server(context));

    final BarrelBlockEntity barrel = context.getBlockEntity(NetworkOperationFixture.EAST_BARREL);
    barrel.clear();
    barrel.markDirty();
    sweeping.sweep(server(context));

    context.assertTrue(!isWarning(context), "The sweep should have cleared the block");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anEmptyNetworkIsLeftAlone(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    sweep(context);

    context.assertTrue(!isWarning(context), "An empty network should leave the block alone");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aSettledNetworkIsPassedOverOnTheNextSweep(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkCapacityWatch watch = new NetworkCapacityWatch();
    TerminalCapacityBroadcast sweeping =
        new TerminalCapacityBroadcast(new NetworkCapacityService(), watch);

    sweeping.sweep(server(context));
    sweeping.sweep(server(context));

    context.assertTrue(
        watch.isWatching(address(context)), "The sweep should remember the network it measured");
    context.assertTrue(
        !watch.needsMeasuring(address(context), network(context).session().invalidationCount()),
        "A settled network should be passed over rather than measured again");
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aTerminalThatLosesPowerIsForgottenByTheSweep(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    NetworkCapacityWatch watch = new NetworkCapacityWatch();
    TerminalCapacityBroadcast sweeping =
        new TerminalCapacityBroadcast(new NetworkCapacityService(), watch);
    sweeping.sweep(server(context));

    TerminalNetworks.power().unloaded(context.getWorld(), terminal(context));
    sweeping.sweep(server(context));

    context.assertTrue(
        !watch.isWatching(address(context)),
        "An unpowered terminal should be forgotten by the sweep");
    NetworkOperationFixture.finish(context, owner);
  }

  private static TerminalAddress address(TestContext context) {
    return TerminalAddress.of(context.getWorld(), terminal(context));
  }

  private static NetworkMembership network(TestContext context) {
    return TerminalNetworks.registry().find(address(context)).orElseThrow();
  }

  private static void sweep(TestContext context) {
    sweeper().sweep(server(context));
  }

  private static TerminalCapacityBroadcast sweeper() {
    return new TerminalCapacityBroadcast(new NetworkCapacityService(), new NetworkCapacityWatch());
  }

  private static net.minecraft.server.MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }

  private static boolean isWarning(TestContext context) {
    return context.getWorld().getBlockState(terminal(context)).get(TerminalBlock.NEAR_CAPACITY);
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }
}
