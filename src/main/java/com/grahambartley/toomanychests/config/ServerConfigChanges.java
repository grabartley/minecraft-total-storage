package com.grahambartley.toomanychests.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.server.MinecraftServer;

/**
 * Who wants to hear that the running server's configuration changed.
 *
 * <p>Sits between the configuration and the code that reacts to it so the configuration never has
 * to know that a network layer exists. Without it, every caller that edits a setting would have to
 * remember to tell the clients afterwards, and the caller that forgets leaves every client silently
 * stale.
 *
 * <p>Server side and single threaded, like the config updates that feed it.
 */
public final class ServerConfigChanges {
  private static final Set<ServerConfigListener> listeners = new LinkedHashSet<>();

  private ServerConfigChanges() {}

  public static void subscribe(ServerConfigListener listener) {
    Objects.requireNonNull(listener, "listener");
    listeners.add(listener);
  }

  public static void unsubscribe(ServerConfigListener listener) {
    listeners.remove(listener);
  }

  public static void publish(MinecraftServer server, TooManyChestsConfig config) {
    if (listeners.isEmpty() || config == null) {
      return;
    }
    for (final ServerConfigListener listener : subscribers()) {
      listener.serverConfigChanged(server, config);
    }
  }

  public static int listenerCount() {
    return listeners.size();
  }

  public static boolean isSubscribed(ServerConfigListener listener) {
    return listeners.contains(listener);
  }

  public static void clear() {
    listeners.clear();
  }

  /** Copied because a listener is entitled to unsubscribe while being told about a change. */
  private static List<ServerConfigListener> subscribers() {
    return List.copyOf(listeners);
  }
}
