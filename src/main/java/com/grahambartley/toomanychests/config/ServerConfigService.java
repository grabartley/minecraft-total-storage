package com.grahambartley.toomanychests.config;

import java.nio.file.Path;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.level.storage.LevelStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerConfigService {
  public static final int OP_PERMISSION_LEVEL = 2;

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerConfigService.class);

  private static volatile TooManyChestsConfig current = TooManyChestsConfig.defaults();

  private ServerConfigService() {}

  public static TooManyChestsConfig current() {
    return current;
  }

  public static void loadFromSession(final LevelStorage.Session session) {
    if (session == null) {
      current = TooManyChestsConfig.defaults();
      return;
    }
    loadFrom(session.getDirectory(WorldSavePath.ROOT).normalize());
  }

  public static void loadFromWorld(final MinecraftServer server) {
    if (server == null) {
      current = TooManyChestsConfig.defaults();
      return;
    }
    loadFrom(server.getSavePath(WorldSavePath.ROOT).normalize());
  }

  public static boolean update(final MinecraftServer server, final TooManyChestsConfig updated) {
    if (server == null || updated == null) {
      return false;
    }
    final Path configPath = pathsFor(server).getServerConfigPath();
    if (!ConfigFile.save(configPath, updated)) {
      return false;
    }
    current = updated;
    ServerConfigChanges.publish(server, updated);
    return true;
  }

  private static void loadFrom(final Path worldRoot) {
    current = ConfigFile.loadOrCreate(new ConfigPaths(worldRoot).getServerConfigPath());
    LOGGER.info(
        "Too Many Chests server config loaded for {}: maxTerminalRadius={}, access={}, ordering={}",
        worldRoot.getFileName(),
        current.maxTerminalRadius(),
        current.defaultTerminalAccessMode().id(),
        current.sortOrdering().id());
  }

  public static ConfigPaths pathsFor(final MinecraftServer server) {
    return new ConfigPaths(server.getSavePath(WorldSavePath.ROOT).normalize());
  }
}
