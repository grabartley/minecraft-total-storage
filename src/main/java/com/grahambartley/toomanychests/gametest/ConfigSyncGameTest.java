package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigListener;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.grahambartley.toomanychests.network.ConfigSync;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;

/**
 * Proves the server decides every configuration edit, including the ones a modified client could
 * invent. Permission levels and the connected player list only exist on a running server, so the
 * gate cannot be checked off-game.
 *
 * <p>Shares the {@code server-config} batch with {@link ServerConfigServiceGameTest} because both
 * move the one live configuration the whole server reads, and tests in a batch run one at a time.
 */
public final class ConfigSyncGameTest implements FabricGameTest {
  private static final String BATCH = "server-config";
  private static final int TICK_LIMIT = 20;
  private static final int EDITED_RADIUS = 32;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anEditFromANonOperatorLeavesTheConfigAlone(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    TooManyChestsConfig original = ServerConfigService.current();

    ConfigSync.handleEdit(player, radiusOf(EDITED_RADIUS));

    context.assertTrue(
        !player.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL),
        "A freshly connected mock player should not be an operator");
    context.assertTrue(
        ServerConfigService.current().equals(original),
        "A non-operator edit must change nothing but read " + ServerConfigService.current());
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anEditFromAnOperatorBecomesTheLiveConfig(TestContext context) {
    MinecraftServer server = context.getWorld().getServer();
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    TooManyChestsConfig original = ServerConfigService.current();
    grantOperator(server, player);

    ConfigSync.handleEdit(player, radiusOf(EDITED_RADIUS));
    int live = ServerConfigService.current().maxTerminalRadius();

    server.getPlayerManager().getOpList().remove(player.getGameProfile());
    ServerConfigService.update(server, original);
    context.assertTrue(
        live == EDITED_RADIUS, "An operator edit should be adopted but the radius read " + live);
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anEditThatIsRefusedIsNotHalfApplied(TestContext context) {
    ServerPlayerEntity player = context.createMockCreativeServerPlayerInWorld();
    TooManyChestsConfig original = ServerConfigService.current();

    ConfigSync.handleEdit(player, null);

    context.assertTrue(
        ServerConfigService.current().equals(original),
        "An edit carrying no config must change nothing but read " + ServerConfigService.current());
    disconnectAndComplete(context, player);
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void everyAcceptedChangeIsAnnouncedOnce(TestContext context) {
    MinecraftServer server = context.getWorld().getServer();
    TooManyChestsConfig original = ServerConfigService.current();
    List<TooManyChestsConfig> heard = new ArrayList<>();
    ServerConfigListener recorder = (changedServer, config) -> heard.add(config);
    ServerConfigChanges.subscribe(recorder);

    ServerConfigService.update(server, radiusOf(EDITED_RADIUS));

    ServerConfigChanges.unsubscribe(recorder);
    ServerConfigService.update(server, original);
    context.assertTrue(
        heard.size() == 1, "One accepted change should be announced once, not " + heard.size());
    context.assertTrue(
        heard.get(0).maxTerminalRadius() == EDITED_RADIUS,
        "Listeners should hear the new values, not the old ones");
    context.complete();
  }

  /**
   * Adds the operator entry by hand rather than through {@code addToOperators}, which stamps the
   * entry with the server's own op level. A test server reports that level as zero, so the
   * convenient call would produce an operator who fails every permission check.
   */
  private static void grantOperator(MinecraftServer server, ServerPlayerEntity player) {
    server
        .getPlayerManager()
        .getOpList()
        .add(
            new OperatorEntry(
                player.getGameProfile(), ServerConfigService.OP_PERMISSION_LEVEL, false));
  }

  private static TooManyChestsConfig radiusOf(int maxTerminalRadius) {
    TooManyChestsConfig defaults = TooManyChestsConfig.defaults();
    return new TooManyChestsConfig(
        maxTerminalRadius,
        defaults.defaultTerminalAccessMode(),
        defaults.sortOrdering(),
        defaults.sortButtonEnabled(),
        defaults.middleClickSortEnabled(),
        defaults.quickStackRadius(),
        defaults.depositAllSkipsHotbar(),
        defaults.nearCapacityWarningPercent(),
        defaults.expensiveOperationCooldownTicks(),
        defaults.movesPerTickBudget(),
        Map.of());
  }

  private static void disconnectAndComplete(TestContext context, ServerPlayerEntity... players) {
    for (final ServerPlayerEntity player : players) {
      context.getWorld().getServer().getPlayerManager().remove(player);
    }
    context.complete();
  }
}
