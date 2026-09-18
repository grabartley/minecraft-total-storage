package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.access.ServerSettingsAccess;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

final class ConfigWriteGate {
  private ConfigWriteGate() {}

  static boolean refuses(CommandContext<ServerCommandSource> context) {
    if (ServerSettingsAccess.mayEdit(context.getSource())) {
      return false;
    }
    context.getSource().sendError(CommandMessages.settingsRefused());
    return true;
  }
}
