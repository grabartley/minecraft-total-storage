package com.grahambartley.totalstorage.config;

public final class ClientConfigMirror {
  private static volatile TotalStorageConfig received;

  private ClientConfigMirror() {}

  public static TotalStorageConfig current() {
    final TotalStorageConfig latest = received;
    return latest == null ? TotalStorageConfig.defaults() : latest;
  }

  public static boolean hasReceived() {
    return received != null;
  }

  public static void accept(TotalStorageConfig config) {
    received = config;
  }

  public static void clear() {
    received = null;
  }
}
