package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.lifecycle.NetworkMembership;
import com.grahambartley.toomanychests.terminal.ContainerEligibilityRefresh;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
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
  private static final String BATCH = "container-eligibility-refresh";
  private static final int TICK_LIMIT = 60;
  private static final String FURNACE = "minecraft:furnace";

  @BeforeBatch(batchId = BATCH)
  public void seedFromTheWorldsConfig(ServerWorld world) {
    ContainerEligibilityRefresh.overrides().seed(ServerConfigService.current());
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void admittingABlockBringsItIntoARunningNetworkWithNoRestart(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    context.setBlockState(NetworkOperationFixture.WEST_BARREL, Blocks.FURNACE.getDefaultState());
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    TooManyChestsConfig original = ServerConfigService.current();

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
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void withdrawingAnOverrideDropsTheBlockFromARunningNetwork(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    context.setBlockState(NetworkOperationFixture.WEST_BARREL, Blocks.FURNACE.getDefaultState());
    TooManyChestsConfig original = ServerConfigService.current();
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
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aConfigChangeLeavingEligibilityAloneDoesNotRebuildTheNetwork(TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner = NetworkOperationFixture.openTerminal(context);
    TooManyChestsConfig original = ServerConfigService.current();
    NetworkMembership before = network(context);

    ServerConfigService.update(
        server(context), original.withContainerEligibilityOverrides(Map.of()));
    NetworkMembership after = network(context);
    ServerConfigService.update(server(context), original);

    context.assertTrue(
        before == after, "A config change that moves no eligibility should scan nothing");
    NetworkOperationFixture.finish(context, owner);
  }

  private static TooManyChestsConfig override(TooManyChestsConfig base, boolean eligible) {
    return base.withContainerEligibilityOverrides(Map.of(FURNACE, eligible));
  }

  private static int containerCount(TestContext context) {
    return TerminalNetworks.power()
        .network(context.getWorld(), terminal(context))
        .map(network -> network.containers().size())
        .orElse(0);
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
