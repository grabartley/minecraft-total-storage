package com.grahambartley.totalstorage.config;

import java.nio.file.Path;

public final class ConfigFile {
  private static final JsonFileStore<TotalStorageConfig> STORE =
      new JsonFileStore<>(
          "config",
          TotalStorageConfig::defaults,
          ConfigSerializer::toJson,
          ConfigSerializer::fromJson);

  private ConfigFile() {}

  public static TotalStorageConfig loadOrCreate(Path path) {
    return STORE.loadOrCreate(path);
  }

  public static TotalStorageConfig load(Path path) {
    return STORE.load(path);
  }

  public static boolean save(Path path, TotalStorageConfig config) {
    return STORE.save(path, config);
  }
}
