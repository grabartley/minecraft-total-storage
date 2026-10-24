package com.grahambartley.totalstorage.config;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.server.MinecraftServer;

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

  public static void publish(MinecraftServer server, TotalStorageConfig config) {
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

  private static List<ServerConfigListener> subscribers() {
    return List.copyOf(listeners);
  }
}
