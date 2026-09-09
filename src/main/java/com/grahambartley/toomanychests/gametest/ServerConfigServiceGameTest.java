package com.grahambartley.toomanychests.gametest;

import com.grahambartley.toomanychests.config.ConfigFile;
import com.grahambartley.toomanychests.config.ConfigPaths;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.SortOrdering;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.server.MinecraftServer;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.util.WorldSavePath;

public final class ServerConfigServiceGameTest implements FabricGameTest {
  private static final String BATCH = "server-config";
  private static final int TICK_LIMIT = 20;

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theRunningWorldOwnsItsOwnConfigFile(TestContext context) {
    Path configPath = configPathFor(context);

    context.assertTrue(
        Files.exists(configPath),
        "Starting a world should leave a config inside its own save at " + configPath);
    context.assertTrue(
        configPath.startsWith(worldRoot(context)),
        "Config should live under the world save, not in a global directory");
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void theLoadedConfigMatchesWhatIsOnDisk(TestContext context) {
    TooManyChestsConfig onDisk = ConfigFile.load(configPathFor(context));

    context.assertTrue(
        ServerConfigService.current().equals(onDisk),
        "Live config " + ServerConfigService.current() + " should match on-disk " + onDisk);
    context.complete();
  }

  @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE, batchId = BATCH, tickLimit = TICK_LIMIT)
  public void anUpdatedSettingBecomesLiveAndIsPersisted(TestContext context) {
    MinecraftServer server = context.getWorld().getServer();
    TooManyChestsConfig original = ServerConfigService.current();

    boolean saved = ServerConfigService.update(server, radiusOf(32));
    int live = ServerConfigService.current().maxTerminalRadius();
    int persisted = ConfigFile.load(configPathFor(context)).maxTerminalRadius();
    ServerConfigService.update(server, original);

    context.assertTrue(saved, "Updating the config should report success");
    context.assertTrue(live == 32, "Live config should read 32 but read " + live);
    context.assertTrue(persisted == 32, "Persisted config should read 32 but read " + persisted);
    context.complete();
  }

  private static TooManyChestsConfig radiusOf(int maxTerminalRadius) {
    return new TooManyChestsConfig(
        maxTerminalRadius,
        TerminalAccessMode.OWNER,
        SortOrdering.REGISTRY_ID,
        true,
        true,
        8,
        false,
        90,
        10,
        64,
        Map.of());
  }

  private static Path worldRoot(TestContext context) {
    return context.getWorld().getServer().getSavePath(WorldSavePath.ROOT).normalize();
  }

  private static Path configPathFor(TestContext context) {
    return new ConfigPaths(worldRoot(context)).getServerConfigPath();
  }
}
