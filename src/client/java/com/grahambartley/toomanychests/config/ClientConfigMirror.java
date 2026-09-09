package com.grahambartley.toomanychests.config;

public final class ClientConfigMirror {
  private static volatile TooManyChestsConfig received;

  private ClientConfigMirror() {}

  public static TooManyChestsConfig current() {
    final TooManyChestsConfig latest = received;
    return latest == null ? TooManyChestsConfig.defaults() : latest;
  }

  public static boolean hasReceived() {
    return received != null;
  }

  public static void accept(TooManyChestsConfig config) {
    received = config;
  }

  public static void clear() {
    received = null;
  }
}
