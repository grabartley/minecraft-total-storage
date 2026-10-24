package com.grahambartley.totalstorage.config;

import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientPreferencesStore {
  private static final String MOD_DIR = "total-storage";
  private static final String CLIENT_FILE = "client.json";

  private static volatile ClientPreferences current = ClientPreferences.defaults();

  private ClientPreferencesStore() {}

  public static void load() {
    load(defaultPath());
  }

  public static void load(Path path) {
    current = ClientPreferencesFile.loadOrCreate(path);
  }

  public static ClientPreferences current() {
    return current;
  }

  public static Path defaultPath() {
    return FabricLoader.getInstance().getConfigDir().resolve(MOD_DIR).resolve(CLIENT_FILE);
  }
}
