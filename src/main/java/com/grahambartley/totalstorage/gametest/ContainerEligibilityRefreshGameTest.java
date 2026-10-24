package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.config.ConfigBuilder;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.config.SortOrdering;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.lifecycle.NetworkMembership;
import com.grahambartley.totalstorage.terminal.ContainerEligibilityRefresh;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.math.BlockPos;

public final class ContainerEligibilityRefreshGameTest implements FabricGameTest {
  private static final String ADMIT_BATCH = "container-eligibility-admit";
  private static final String WITHDRAW_BATCH = "container-eligibility-withdraw";
  private static final String UNTOUCHED_BATCH = "container-eligibility-untouched";
  private static final int TICK_LIMIT = 60;
  private static final String FURNACE = "minecraft:furnace";

  @BeforeBatch(batchId = ADMIT_BATCH)
  public void seedBeforeAdmitting(ServerWorld world) {
    ContainerEligibilityRefresh.seedFrom(ServerConfigService.current());
  }

  @BeforeBatch(batchId = WITHDRAW_BATCH)
  public void seedBeforeWithdrawing(ServerWorld world) {
    ContainerEligibilityRefresh.seedFrom(ServerConfigService.current());
  }

  @BeforeBatch(batchId = UNTOUCHED_BATCH)
  public void seedBeforeLeavingEligibilityAlone(ServerWorld world) {
    ContainerEligibilityRefresh.seedFrom(ServerConfigService.current());
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = ADMIT_BATCH,
      tickLimit = TICK_LIMIT)
  public void admittingABlockBringsItIntoARunningNetworkWithNoRestart(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    context.setBlockState(NetworkOperationFixture.WEST_BARREL, Blocks.FURNACE.getDefaultState());
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    TotalStorageConfig original = ServerConfigService.current();

    int denied = containerCount(context);
    ServerConfigService.update(server(context), override(original, true));
    int admitted = containerCount(context);
    ServerConfigService.update(server(context), original);

    context.assertTrue(denied == 1, "A furnace should start outside the network, held " + denied);
    context.assertTrue(
        admitted == 2,
        "Admitting the furnace should have brought it in, the network held " + admitted);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = WITHDRAW_BATCH,
      tickLimit = TICK_LIMIT)
  public void withdrawingAnOverrideDropsTheBlockFromARunningNetwork(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    context.setBlockState(NetworkOperationFixture.WEST_BARREL, Blocks.FURNACE.getDefaultState());
    TotalStorageConfig original = ServerConfigService.current();
    ServerConfigService.update(server(context), override(original, true));
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);

    int admitted = containerCount(context);
    ServerConfigService.update(server(context), original);
    int withdrawn = containerCount(context);

    context.assertTrue(admitted == 2, "The override should have admitted the furnace");
    context.assertTrue(
        withdrawn == 1,
        "Withdrawing the override should have dropped the furnace, the network held " + withdrawn);
    NetworkOperationFixture.finish(context, owner);
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = UNTOUCHED_BATCH,
      tickLimit = TICK_LIMIT)
  public void aConfigChangeLeavingEligibilityAloneDoesNotRebuildTheNetwork(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    TotalStorageConfig original = ServerConfigService.current();
    NetworkMembership before = network(context);

    ServerConfigService.update(server(context), withOtherOrdering(original));
    NetworkMembership after = network(context);
    ServerConfigService.update(server(context), original);

    context.assertTrue(
        before == after, "A config change that moves no eligibility should scan nothing");
    NetworkOperationFixture.finish(context, owner);
  }

  private static TotalStorageConfig withOtherOrdering(TotalStorageConfig base) {
    return ConfigBuilder.from(base)
        .sortOrdering(
            base.sortOrdering() == SortOrdering.REGISTRY_ID
                ? SortOrdering.DISPLAY_NAME
                : SortOrdering.REGISTRY_ID)
        .build();
  }

  private static TotalStorageConfig override(TotalStorageConfig base, boolean eligible) {
    return base.withContainerEligibilityOverrides(Map.of(FURNACE, eligible));
  }

  private static int containerCount(TestContext context) {
    return TerminalNetworks.power().containerCount(context.getWorld(), terminal(context));
  }

  private static NetworkMembership network(TestContext context) {
    return TerminalNetworks.power().network(context.getWorld(), terminal(context)).orElseThrow();
  }

  private static BlockPos terminal(TestContext context) {
    return context.getAbsolutePos(NetworkOperationFixture.TERMINAL);
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }
}
