package com.grahambartley.totalstorage.config;

import java.nio.file.Path;

public final class ClientPreferencesFile {
  private static final JsonFileStore<ClientPreferences> STORE =
      new JsonFileStore<>(
          "client preferences",
          ClientPreferences::defaults,
          ClientPreferencesSerializer::toJson,
          ClientPreferencesSerializer::fromJson);

  private ClientPreferencesFile() {}

  public static ClientPreferences loadOrCreate(Path path) {
    return STORE.loadOrCreate(path);
  }

  public static ClientPreferences load(Path path) {
    return STORE.load(path);
  }

  public static boolean save(Path path, ClientPreferences preferences) {
    return STORE.save(path, preferences);
  }
}
