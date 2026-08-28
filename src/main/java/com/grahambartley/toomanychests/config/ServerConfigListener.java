package com.grahambartley.toomanychests.config;

import net.minecraft.server.MinecraftServer;

/**
 * Told that the running server's configuration changed, so live state can follow it.
 *
 * <p>Carries the server as well as the new values because the interesting reactions, telling
 * connected players, are things only the server can do, and a listener that had to reach for a
 * server reference of its own would be free to react to a config that is not the one that changed.
 */
@FunctionalInterface
public interface ServerConfigListener {
  void serverConfigChanged(MinecraftServer server, TooManyChestsConfig config);
}
