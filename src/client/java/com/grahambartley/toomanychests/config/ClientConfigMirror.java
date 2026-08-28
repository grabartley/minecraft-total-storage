package com.grahambartley.toomanychests.config;

/**
 * The last server configuration this client was told about.
 *
 * <p>Presentation only. Nothing here decides whether an operation is allowed; the server re-reads
 * its own configuration for every operation, so a client that rewrote this holder would change what
 * its own screens draw and nothing else.
 *
 * <p>Reads before the first sync arrives, and after leaving a server, answer with the shipped
 * defaults rather than nothing, because a screen drawn during that window should look ordinary
 * rather than fail.
 */
public final class ClientConfigMirror {
  private static volatile TooManyChestsConfig received;

  private ClientConfigMirror() {}

  public static TooManyChestsConfig current() {
    final TooManyChestsConfig latest = received;
    return latest == null ? TooManyChestsConfig.defaults() : latest;
  }

  /** Whether a server has actually sent its configuration, as opposed to defaults standing in. */
  public static boolean hasReceived() {
    return received != null;
  }

  public static void accept(TooManyChestsConfig config) {
    received = config;
  }

  /** Dropped on disconnect so one server's settings never colour the next server's screens. */
  public static void clear() {
    received = null;
  }
}
