package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.access.TerminalCaller;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

final class TerminalCommandGate {
  private TerminalCommandGate() {}

  static TerminalCaller caller(ServerCommandSource source) {
    return TerminalCaller.of(source.getPlayer(), TotalStorageCommand.isOperator(source));
  }

  static Optional<TerminalSettings> administrable(ServerCommandSource source, BlockPos pos) {
    return permitted(source, pos, caller(source)::mayAdminister);
  }

  static Optional<TerminalSettings> openable(ServerCommandSource source, BlockPos pos) {
    return permitted(source, pos, caller(source)::mayOpen);
  }

  private static Optional<TerminalSettings> permitted(
      ServerCommandSource source, BlockPos pos, Predicate<TerminalSettings> permission) {
    final Optional<TerminalSettings> settings = TerminalLookup.settingsAt(source.getWorld(), pos);
    if (settings.isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
      return Optional.empty();
    }
    if (!permission.test(settings.get())) {
      source.sendError(CommandMessages.terminalNotYours(pos));
      return Optional.empty();
    }
    return settings;
  }
}
