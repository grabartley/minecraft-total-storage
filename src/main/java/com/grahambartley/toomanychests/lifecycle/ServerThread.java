package com.grahambartley.toomanychests.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

/**
 * Whether the caller is on the thread that owns world state.
 *
 * <p>Held as a field rather than reached for through a world, because the code that needs to ask is
 * exactly the code that must not assume it has a valid world reference. Volatile because the
 * question is asked from other threads, which is the whole point of asking it.
 */
public final class ServerThread {
  private static volatile MinecraftServer server;

  private ServerThread() {}

  public static void register() {
    ServerLifecycleEvents.SERVER_STARTED.register(started -> server = started);
    ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> server = null);
  }

  /** False before the server starts and after it stops, when there is no world state to own. */
  public static boolean isCurrent() {
    final MinecraftServer running = server;
    return running != null && running.isOnThread();
  }
}
