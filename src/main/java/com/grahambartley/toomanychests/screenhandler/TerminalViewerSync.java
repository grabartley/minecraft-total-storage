package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Reaches every screen currently showing one terminal.
 *
 * <p>Two players at one terminal hold two separate handlers over one network, so an operation
 * committed by either changes what both are looking at. The one who did not act would otherwise
 * keep clicking a listing that no longer matches the network, and per ADR 0009 every one of those
 * clicks is a request the server has to refuse and then resync anyway.
 *
 * <p>Which players to reach comes from the viewer list rather than from a walk of everyone online,
 * so the cost is the number of people at that terminal rather than the size of the server. A viewer
 * whose handler turns out to be something else has already moved on and is dropped from the list
 * here, which is the one place that mismatch can be seen.
 */
public final class TerminalViewerSync {

  private TerminalViewerSync() {}

  public static List<TerminalScreenHandler> openOn(
      MinecraftServer server, TerminalAddress terminal) {
    final List<TerminalScreenHandler> open = new ArrayList<>();
    for (final ServerPlayerEntity player : playersOn(server, terminal)) {
      open.add((TerminalScreenHandler) player.currentScreenHandler);
    }
    return List.copyOf(open);
  }

  /**
   * The players really looking at this terminal right now. Anything that has to send a packet needs
   * the player rather than the handler, and both answers come from the same walk so neither can
   * disagree with the other about who is watching.
   */
  public static List<ServerPlayerEntity> playersOn(
      MinecraftServer server, TerminalAddress terminal) {
    if (server == null || terminal == null) {
      return List.of();
    }
    final List<ServerPlayerEntity> watching = new ArrayList<>();
    for (final UUID viewer : TerminalAccessControl.viewers().viewing(terminal)) {
      final ServerPlayerEntity player = server.getPlayerManager().getPlayer(viewer);
      if (player != null
          && player.currentScreenHandler instanceof TerminalScreenHandler handler
          && handler.terminal().equals(terminal.pos())) {
        watching.add(player);
      } else {
        TerminalAccessControl.viewers().closed(terminal, viewer);
      }
    }
    return List.copyOf(watching);
  }

  /** Returns how many screens were resent, which is what a caller logs or asserts on. */
  public static int resync(MinecraftServer server, TerminalAddress terminal) {
    final List<TerminalScreenHandler> open = openOn(server, terminal);
    open.forEach(TerminalScreenHandler::syncState);
    return open.size();
  }
}
