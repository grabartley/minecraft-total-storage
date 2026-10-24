package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.terminal.ContainerRoleService;
import com.grahambartley.totalstorage.terminal.NetworkCapacity;
import com.grahambartley.totalstorage.terminal.NetworkCapacityService;
import com.grahambartley.totalstorage.terminal.NetworkSettingsService;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class NetworkCapacityServiceGameTest implements FabricGameTest {
  private static final String BATCH = "network-capacity-service";
  private static final int TICK_LIMIT = 60;

  private final NetworkCapacityService capacities = new NetworkCapacityService();
  private final NetworkSettingsService settings = new NetworkSettingsService();
  private final ContainerRoleService roles = new ContainerRoleService();

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void theBlockAndTheScreenReadTheSameCapacity(TestContext context) {
    NetworkOperationFixture.stockedBarrel(
        context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND, 12);
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.WEST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    NetworkCapacity measured = capacities.measure(context.getWorld(), terminal(context));
    NetworkCapacity shown = settings.viewFor(owner, terminal(context), null).capacity();

    context.assertTrue(
        measured.equals(shown),
        "The block and the screen disagreed, " + measured + " against " + shown);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aFullNetworkMeasuresAsFull(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    NetworkCapacity measured = capacities.measure(context.getWorld(), terminal(context));

    context.assertTrue(measured.isFull(), "A full network should measure full, got " + measured);
    context.assertTrue(
        measured.isNearCapacity(90), "A full network is past any threshold, got " + measured);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void anExcludedContainerIsLeftOutOfTheMeasurement(TestContext context) {
    NetworkOperationFixture.fullBarrel(context, NetworkOperationFixture.EAST_BARREL, Items.DIAMOND);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    roles.assign(
        context.getWorld(),
        terminal(context),
        TerminalCaller.of(owner),
        context.getAbsolutePos(NetworkOperationFixture.EAST_BARREL),
        ContainerRole.EXCLUDED);

    NetworkCapacity measured = capacities.measure(context.getWorld(), terminal(context));

    context.assertTrue(
        measured.isUnknown(),
        "Only an excluded container is left, so there is nothing to measure, got " + measured);
    NetworkOperationFixture.finish(context, owner);
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }
}
