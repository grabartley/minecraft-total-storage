package com.grahambartley.totalstorage.gametest;

import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenHandler;
import com.grahambartley.totalstorage.terminal.ContainerEligibilityRefresh;
import com.grahambartley.totalstorage.terminal.TerminalNetworks;
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
  private static final int SETTLE_TICKS = 4;

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
    TotalStorageConfig original = ServerConfigService.current();
    ServerConfigService.update(server(context), excludeBarrels(original));

    context.runAtTick(
        context.getTick() + SETTLE_TICKS,
        () -> {
          long after = handler.listing().revision();
          int held =
              TerminalNetworks.power()
                  .containerCount(
                      context.getWorld(), context.getAbsolutePos(NetworkOperationFixture.TERMINAL));
          ServerConfigService.update(server(context), original);

          context.assertTrue(held == 0, "Excluding barrels should have emptied the network");
          context.assertTrue(
              after > before,
              "A watching player should have been pushed the rebuilt network, stamp stayed at "
                  + before);
          NetworkOperationFixture.finish(context, owner);
        });
  }

  private static TotalStorageConfig excludeBarrels(TotalStorageConfig base) {
    return base.withContainerEligibilityOverrides(Map.of("minecraft:barrel", false));
  }

  private static MinecraftServer server(TestContext context) {
    return context.getWorld().getServer();
  }
}
