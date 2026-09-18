package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.config.ContainerEligibilityOverrides;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Map;
import java.util.Optional;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;

public final class EligibilityCommand {
  private static final String BLOCK = "block";
  private static final String ELIGIBLE = "eligible";

  private EligibilityCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("eligibility")
        .executes(EligibilityCommand::list)
        .then(CommandManager.literal("list").executes(EligibilityCommand::list))
        .then(
            CommandManager.literal("set")
                .then(
                    blockArgument()
                        .then(
                            CommandManager.argument(ELIGIBLE, BoolArgumentType.bool())
                                .executes(EligibilityCommand::set))))
        .then(
            CommandManager.literal("clear")
                .then(CommandManager.literal("all").executes(EligibilityCommand::clearAll))
                .then(blockArgument().executes(EligibilityCommand::clear)));
  }

  private static com.mojang.brigadier.builder.RequiredArgumentBuilder<
          ServerCommandSource, Identifier>
      blockArgument() {
    return CommandManager.argument(BLOCK, IdentifierArgumentType.identifier())
        .suggests(
            (context, builder) ->
                CommandSource.suggestIdentifiers(Registries.BLOCK.getIds(), builder));
  }

  private static int list(CommandContext<ServerCommandSource> context) {
    final ServerCommandSource source = context.getSource();
    final Map<String, Boolean> overrides =
        ServerConfigService.current().containerEligibilityOverrides();
    if (overrides.isEmpty()) {
      source.sendFeedback(CommandMessages::eligibilityEmpty, false);
      return 1;
    }
    source.sendFeedback(CommandMessages::eligibilityHeader, false);
    overrides.forEach(
        (blockId, eligible) ->
            source.sendFeedback(() -> CommandMessages.eligibilityLine(blockId, eligible), false));
    return overrides.size();
  }

  private static int set(CommandContext<ServerCommandSource> context) {
    if (ConfigWriteGate.refuses(context)) {
      return 0;
    }
    final String blockId = blockIdOf(context);
    final boolean eligible = BoolArgumentType.getBool(context, ELIGIBLE);
    final Optional<TooManyChestsConfig> updated =
        ContainerEligibilityOverrides.set(ServerConfigService.current(), blockId, eligible);
    if (updated.isEmpty()) {
      context.getSource().sendError(CommandMessages.eligibilityUnusableBlock(blockId));
      return 0;
    }
    return ConfigCommand.apply(
        context, updated.get(), CommandMessages.eligibilitySet(blockId, eligible));
  }

  private static int clear(CommandContext<ServerCommandSource> context) {
    if (ConfigWriteGate.refuses(context)) {
      return 0;
    }
    final String blockId = blockIdOf(context);
    final Optional<TooManyChestsConfig> updated =
        ContainerEligibilityOverrides.clear(ServerConfigService.current(), blockId);
    if (updated.isEmpty()) {
      context.getSource().sendError(CommandMessages.eligibilityNotOverridden(blockId));
      return 0;
    }
    return ConfigCommand.apply(context, updated.get(), CommandMessages.eligibilityCleared(blockId));
  }

  private static int clearAll(CommandContext<ServerCommandSource> context) {
    if (ConfigWriteGate.refuses(context)) {
      return 0;
    }
    return ConfigCommand.apply(
        context,
        ContainerEligibilityOverrides.clearAll(ServerConfigService.current()),
        CommandMessages.everyEligibilityCleared());
  }

  private static String blockIdOf(CommandContext<ServerCommandSource> context) {
    return IdentifierArgumentType.getIdentifier(context, BLOCK).toString();
  }
}
