package com.grahambartley.toomanychests.config;

import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

public final class ConfigPaths {
  private static final String MOD_DIR = "too-many-chests";
  private static final String SERVER_CONFIG_FILE = "server-config.json";
  private static final String TAG_SET_DIR = "tag-sets";
  private static final String JSON_EXT = ".json";
  private static final String BROKEN_PREFIX = ".broken.";

  private final Path modDir;

  public ConfigPaths(Path worldDir) {
    this.modDir = worldDir.resolve(MOD_DIR);
  }

  public Path getModDir() {
    return modDir;
  }

  public Path getServerConfigPath() {
    return modDir.resolve(SERVER_CONFIG_FILE);
  }

  public Path getTagSetDir() {
    return modDir.resolve(TAG_SET_DIR);
  }

  public Path getTagSetPath(String name) {
    return getTagSetDir().resolve(name + JSON_EXT);
  }

  public static Path brokenBackupPath(Path originalFile, Instant at) {
    final String fileName = originalFile.getFileName().toString();
    final String baseName =
        fileName.endsWith(JSON_EXT)
            ? fileName.substring(0, fileName.length() - JSON_EXT.length())
            : fileName;
    final String timestamp = DateTimeFormatter.ISO_INSTANT.format(at).replace(":", "-");
    return originalFile.resolveSibling(baseName + BROKEN_PREFIX + timestamp + JSON_EXT);
  }
}
