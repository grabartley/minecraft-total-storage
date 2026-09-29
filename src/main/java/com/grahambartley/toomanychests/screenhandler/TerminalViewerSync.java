package com.grahambartley.toomanychests.screenhandler;

import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.network.TerminalSettingsNetworking;
import com.grahambartley.toomanychests.operation.TerminalResync;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

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

  public static int refresh(MinecraftServer server, TerminalAddress terminal) {
    final List<ServerPlayerEntity> watching = playersOn(server, terminal);
    for (final ServerPlayerEntity viewer : watching) {
      TerminalSettingsNetworking.panels().refresh(viewer);
      TerminalResync.contents(viewer);
    }
    return watching.size();
  }

  public static int resync(MinecraftServer server, TerminalAddress terminal) {
    final List<TerminalScreenHandler> open = openOn(server, terminal);
    open.forEach(TerminalScreenHandler::syncState);
    return open.size();
  }
}
