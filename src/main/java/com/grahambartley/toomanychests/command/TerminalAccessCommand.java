package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.access.TerminalAccessControl;
import com.grahambartley.toomanychests.access.TerminalAccessRules;
import com.grahambartley.toomanychests.access.TerminalTrustService;
import com.grahambartley.toomanychests.config.TerminalAccessMode;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public final class TerminalAccessCommand {
  private static final String MODE = "mode";
  private static final String PLAYERS = "players";

  private static final List<String> MODE_NAMES =
      Arrays.stream(TerminalAccessMode.values()).map(TerminalAccessMode::id).toList();

  private static final SuggestionProvider<ServerCommandSource> MODE_SUGGESTIONS =
      (context, builder) -> CommandSource.suggestMatching(MODE_NAMES, builder);

  private TerminalAccessCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> accessNode() {
    return CommandManager.literal("access")
        .executes(TerminalAccessCommand::getAccessMode)
        .then(
            CommandManager.argument(MODE, StringArgumentType.word())
                .suggests(MODE_SUGGESTIONS)
                .executes(TerminalAccessCommand::setAccessMode));
  }

  static LiteralArgumentBuilder<ServerCommandSource> trustNode() {
    return CommandManager.literal("trust")
        .executes(TerminalAccessCommand::listTrusted)
        .then(
            CommandManager.argument(PLAYERS, GameProfileArgumentType.gameProfile())
                .executes(TerminalAccessCommand::trust));
  }

  static LiteralArgumentBuilder<ServerCommandSource> untrustNode() {
    return CommandManager.literal("untrust")
        .then(
            CommandManager.argument(PLAYERS, GameProfileArgumentType.gameProfile())
                .executes(TerminalAccessCommand::untrust));
  }

  private static int getAccessMode(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    final Optional<TerminalSettings> settings = trustService().settingsAt(source.getWorld(), pos);
    if (settings.isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
      return 0;
    }
    source.sendFeedback(
        () -> CommandMessages.terminalAccessMode(pos, settings.get().accessMode()), false);
    return 1;
  }

  private static int setAccessMode(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    if (!administrable(context, pos)) {
      return 0;
    }
    final String requested = StringArgumentType.getString(context, MODE);
    final Optional<TerminalAccessMode> mode = TerminalAccessMode.fromId(requested);
    if (mode.isEmpty()) {
      source.sendError(CommandMessages.unknownAccessMode(requested, String.join(", ", MODE_NAMES)));
      return 0;
    }
    if (!trustService().setAccessMode(source.getWorld(), pos, mode.get())) {
      source.sendFeedback(
          () -> CommandMessages.terminalAccessModeUnchanged(pos, mode.get()), false);
      return 0;
    }
    source.sendFeedback(() -> CommandMessages.terminalAccessModeSet(pos, mode.get()), true);
    return 1;
  }

  private static int listTrusted(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    if (!administrable(context, pos)) {
      return 0;
    }
    final TerminalSettings settings =
        trustService().settingsAt(source.getWorld(), pos).orElseThrow();
    if (settings.trusted().isEmpty()) {
      source.sendFeedback(() -> CommandMessages.terminalTrustEmpty(pos), false);
      return 0;
    }
    source.sendFeedback(() -> CommandMessages.terminalTrustHeader(pos), false);
    for (final UUID trusted : settings.trusted()) {
      source.sendFeedback(() -> CommandMessages.terminalTrustEntry(nameOf(source, trusted)), false);
    }
    return settings.trusted().size();
  }

  private static int trust(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    return editTrust(context, true);
  }

  private static int untrust(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    return editTrust(context, false);
  }

  private static int editTrust(CommandContext<ServerCommandSource> context, boolean adding)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    if (!administrable(context, pos)) {
      return 0;
    }
    final ServerWorld world = source.getWorld();
    final Collection<GameProfile> profiles =
        GameProfileArgumentType.getProfileArgument(context, PLAYERS);
    int changed = 0;
    for (final GameProfile profile : profiles) {
      final boolean moved =
          adding
              ? trustService().trust(world, pos, profile.getId())
              : trustService().untrust(world, pos, profile.getId());
      changed += moved ? 1 : 0;
      source.sendFeedback(() -> feedback(pos, profile.getName(), adding, moved), moved);
    }
    return changed;
  }

  private static net.minecraft.text.Text feedback(
      BlockPos pos, String player, boolean adding, boolean moved) {
    if (adding) {
      return moved
          ? CommandMessages.terminalTrusted(player, pos)
          : CommandMessages.terminalAlreadyTrusted(player, pos);
    }
    return moved
        ? CommandMessages.terminalUntrusted(player, pos)
        : CommandMessages.terminalNotTrusted(player, pos);
  }

  private static boolean administrable(CommandContext<ServerCommandSource> context, BlockPos pos) {
    final ServerCommandSource source = context.getSource();
    final Optional<TerminalSettings> settings = trustService().settingsAt(source.getWorld(), pos);
    if (settings.isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
      return false;
    }
    final UUID caller = source.getPlayer() == null ? null : source.getPlayer().getUuid();
    if (TerminalAccessRules.mayAdminister(
        settings.get(), caller, TooManyChestsCommand.isOperator(source))) {
      return true;
    }
    source.sendError(CommandMessages.terminalNotYours(pos));
    return false;
  }

  private static String nameOf(ServerCommandSource source, UUID player) {
    if (source.getServer().getUserCache() == null) {
      return player.toString();
    }
    return source
        .getServer()
        .getUserCache()
        .getByUuid(player)
        .map(GameProfile::getName)
        .orElseGet(player::toString);
  }

  private static TerminalTrustService trustService() {
    return TerminalAccessControl.trust();
  }
}
