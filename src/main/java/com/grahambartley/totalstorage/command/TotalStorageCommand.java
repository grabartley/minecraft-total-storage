package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.config.ServerConfigService;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class TotalStorageCommand {
  private static final String ROOT = "totalstorage";
  private static final String ALIAS = "ts";

  private TotalStorageCommand() {}

  public static void register() {
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> register(dispatcher));
  }

  static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
    final LiteralCommandNode<ServerCommandSource> root =
        dispatcher.register(
            CommandManager.literal(ROOT)
                .executes(TotalStorageCommand::help)
                .then(ConfigCommand.node())
                .then(TerminalCommand.node())
                .then(SortCommand.node())
                .then(QuickStackCommand.node()));
    dispatcher.register(
        CommandManager.literal(ALIAS).executes(TotalStorageCommand::help).redirect(root));
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
        HelpLine.of(CommandMessages.helpConfig(), "config"),
        HelpLine.of(CommandMessages.helpEligibility(), "config eligibility"),
        HelpLine.of(CommandMessages.helpTerminal(), "terminal radius"),
        HelpLine.of(CommandMessages.helpTerminalAccess(), "terminal access"),
        HelpLine.of(CommandMessages.helpTerminalTrust(), "terminal trust", "terminal untrust"),
        HelpLine.of(CommandMessages.helpTerminalInfo(), "terminal info", "terminal rescan"),
        HelpLine.of(CommandMessages.helpTerminalRole(), "terminal role"),
        HelpLine.of(CommandMessages.helpTerminalName(), "terminal name"),
        HelpLine.of(CommandMessages.helpTerminalFilter(), "terminal filter"),
        HelpLine.of(CommandMessages.helpTerminalHighlight(), "terminal highlight"),
        HelpLine.of(TagSetMessages.help(), "terminal tags"),
        HelpLine.of(CommandMessages.helpSort(), "sort"),
        HelpLine.of(CommandMessages.helpQuickStack(), "quickstack"));
  }

  record HelpLine(Text text, Set<String> commands) {
    static HelpLine of(Text text, String... commands) {
      return new HelpLine(text, Set.of(commands));
    }
  }
}
