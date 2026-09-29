package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

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
                .then(SortCommand.node())
                .then(QuickStackCommand.node()));
    dispatcher.register(
        CommandManager.literal(ALIAS).executes(TooManyChestsCommand::help).redirect(root));
  }

  static boolean isOperator(ServerCommandSource source) {
    return source.hasPermissionLevel(ServerConfigService.OP_PERMISSION_LEVEL);
  }

  private static int help(CommandContext<ServerCommandSource> context) {
    final ServerCommandSource source = context.getSource();
    for (final HelpLine line : helpLines()) {
      source.sendFeedback(line::text, false);
    }
    return 1;
  }

  static List<HelpLine> helpLines() {
    return List.of(
        HelpLine.of(CommandMessages.helpHeader()),
        HelpLine.of(CommandMessages.helpConfig()),
        HelpLine.of(CommandMessages.helpEligibility()),
        HelpLine.terminal(CommandMessages.helpTerminal(), "radius"),
        HelpLine.terminal(CommandMessages.helpTerminalAccess(), "access"),
        HelpLine.terminal(CommandMessages.helpTerminalTrust(), "trust", "untrust"),
        HelpLine.terminal(CommandMessages.helpTerminalInfo(), "info", "rescan"),
        HelpLine.terminal(CommandMessages.helpTerminalRole(), "role"),
        HelpLine.terminal(CommandMessages.helpTerminalName(), "name"),
        HelpLine.terminal(CommandMessages.helpTerminalFilter(), "filter"),
        HelpLine.terminal(CommandMessages.helpTerminalHighlight(), "highlight"),
        HelpLine.terminal(TagSetMessages.help(), "tags"),
        HelpLine.of(CommandMessages.helpSort()),
        HelpLine.of(CommandMessages.helpQuickStack()));
  }

  record HelpLine(Text text, Set<String> terminalSubcommands) {
    static HelpLine of(Text text) {
      return new HelpLine(text, Set.of());
    }

    static HelpLine terminal(Text text, String... subcommands) {
      return new HelpLine(text, Set.of(subcommands));
    }
  }
}
