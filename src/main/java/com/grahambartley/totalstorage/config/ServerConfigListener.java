package com.grahambartley.totalstorage.config;

import net.minecraft.server.MinecraftServer;

@FunctionalInterface
public interface ServerConfigListener {
  void serverConfigChanged(MinecraftServer server, TotalStorageConfig config);
}
