package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.screenhandler.TerminalScreenHandler;
import com.grahambartley.toomanychests.terminal.ContainerEligibilityRefresh;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.BeforeBatch;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

public final class TerminalPanelRefreshGameTest implements FabricGameTest {
  private static final String BATCH = "terminal-panel-refresh";
  private static final int TICK_LIMIT = 60;

  @BeforeBatch(batchId = BATCH)
  public void seedFromTheWorldsConfig(ServerWorld world) {
    ContainerEligibilityRefresh.seedFrom(ServerConfigService.current());
  }

  @GameTest(
      templateName = NetworkOperationFixture.CONTAINER_PAD,
      batchId = BATCH,
      tickLimit = TICK_LIMIT)
  public void aConfigChangeThatRebuildsANetworkPushesAFreshPageToEveryoneWatching(
      TestContext context) {
    NetworkOperationFixture.placeBarrel(context, NetworkOperationFixture.EAST_BARREL);
    ServerPlayerEntity owner =
        NetworkOperationFixture.watching(NetworkOperationFixture.openTerminal(context));
    TerminalScreenHandler handler = NetworkOperationFixture.handlerOf(owner);
    long before = handler.listing().revision();
    TooManyChestsConfig original = ServerConfigService.current();

    ServerConfigService.update(server(context), excludeBarrels(original));
    long after = handler.listing().revision();
    ServerConfigService.update(server(context), original);

    context.assertTrue(
        after > before,
        "A watching player should have been pushed the rebuilt network, stamp stayed at " + before);
    NetworkOperationFixture.finish(context, owner);
  }

  private static TooManyChestsConfig excludeBarrels(TooManyChestsConfig base) {
    return base.withContainerEligibilityOverrides(Map.of("minecraft:barrel", false));
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }
}
