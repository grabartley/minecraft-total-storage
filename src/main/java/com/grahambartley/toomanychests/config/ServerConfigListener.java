package com.grahambartley.toomanychests.config;

import net.minecraft.server.MinecraftServer;

@FunctionalInterface
public interface ServerConfigListener {
  void serverConfigChanged(MinecraftServer server, TooManyChestsConfig config);
}
