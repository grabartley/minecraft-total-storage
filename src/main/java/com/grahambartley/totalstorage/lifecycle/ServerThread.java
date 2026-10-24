package com.grahambartley.totalstorage.lifecycle;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;

public final class ServerThread {
  private static volatile MinecraftServer server;

  private ServerThread() {}

  public static void register() {
    ServerLifecycleEvents.SERVER_STARTED.register(started -> server = started);
    ServerLifecycleEvents.SERVER_STOPPED.register(stopped -> server = null);
  }

  public static boolean isCurrent() {
    final MinecraftServer running = server;
    return running != null && running.isOnThread();
  }
}
