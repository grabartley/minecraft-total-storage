package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.config.ServerConfigChanges;
import com.grahambartley.toomanychests.config.ServerConfigListener;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import java.util.Objects;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

/**
 * Makes a move in the server's maximum radius reach the terminals that are already running.
 *
 * <p>Without this a lowered maximum would only bind a terminal that happened to be repowered
 * afterwards, which is no cap at all on a base nobody touches. What each terminal asked for is left
 * alone, so raising the maximum again hands its reach back, and only a terminal whose reach really
 * moved pays for a scan.
 */
public final class TerminalRadiusClamp implements ServerConfigListener {
  private final TerminalPower power;

  public TerminalRadiusClamp(TerminalPower power) {
    this.power = Objects.requireNonNull(power, "power");
  }

  public static void register() {
    ServerConfigChanges.subscribe(new TerminalRadiusClamp(TerminalNetworks.power()));
  }

  @Override
  public void serverConfigChanged(MinecraftServer server, TooManyChestsConfig config) {
    if (server == null || config == null) {
      return;
    }
    for (final TerminalAddress address : power.registry().addresses()) {
      final ServerWorld world = server.getWorld(address.worldKey());
      if (world != null) {
        power.rescan(world, address.pos());
      }
    }
  }
}
