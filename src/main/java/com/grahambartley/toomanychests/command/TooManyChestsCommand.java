package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * The root of the mod's command tree, and the only place its permission level is decided.
 *
 * <p>Every setting is reachable here, because a server owner over SSH with no client mod installed
 * is the case the configuration has to serve first. A settings screen is a convenience layered on
 * top of these commands rather than a second way in with rules of its own.
 */
public final class TooManyChestsCommand {
  private static final String ROOT = "toomanychests";
  private static final String ALIAS = "tmc";

  private TooManyChestsCommand() {}

  public static void register() {
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> register(dispatcher));
  }

  static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    final LiteralCommandNode<ServerCommandSource> root =
        dispatcher.register(
            CommandManager.literal(ROOT)
                .executes(TooManyChestsCommand::help)
                .then(ConfigCommand.node())
                .then(TerminalCommand.node())
                .then(SortCommand.node()));
    dispatcher.register(
        CommandManager.literal(ALIAS).executes(TooManyChestsCommand::help).redirect(root));
  }

  static boolean isOperator(ServerCommandSource source) {
    return source.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL);
  }

  private static int help(CommandContext<ServerCommandSource> context) {
    final ServerCommandSource source = context.getSource();
    for (final Text line : helpLines()) {
      source.sendFeedback(() -> line, false);
    }
    return 1;
  }

  private static List<Text> helpLines() {
    return List.of(
        CommandMessages.helpHeader(),
        CommandMessages.helpConfig(),
        CommandMessages.helpEligibility(),
        CommandMessages.helpTerminal(),
        CommandMessages.helpTerminalAccess(),
        CommandMessages.helpTerminalTrust(),
        CommandMessages.helpTerminalInfo(),
        CommandMessages.helpTerminalRole(),
        CommandMessages.helpTerminalHighlight(),
        CommandMessages.helpSort());
  }
}
