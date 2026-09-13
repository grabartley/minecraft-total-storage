package com.grahambartley.toomanychests.config;

import java.nio.file.Path;

public final class ConfigFile {
  private static final JsonFileStore<TooManyChestsConfig> STORE =
      new JsonFileStore<>(
          "config",
          TooManyChestsConfig::defaults,
          ConfigSerializer::toJson,
          ConfigSerializer::fromJson);

  private ConfigFile() {}

  public static TooManyChestsConfig loadOrCreate(Path path) {
    return STORE.loadOrCreate(path);
  }

  public static TooManyChestsConfig load(Path path) {
    return STORE.load(path);
  }

  public static boolean save(Path path, TooManyChestsConfig config) {
    return STORE.save(path, config);
  }
}
