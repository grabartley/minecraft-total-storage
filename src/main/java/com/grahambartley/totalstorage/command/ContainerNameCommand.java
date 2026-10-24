package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.screenhandler.TerminalViewerSync;
import com.grahambartley.totalstorage.storage.ContainerNameRule;
import com.grahambartley.totalstorage.terminal.ContainerNamingService;
import com.grahambartley.totalstorage.terminal.RoleChangeOutcome;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public final class ContainerNameCommand {
  private static final String CONTAINER = "container";
  private static final String NAME = "name";

  private static final ContainerNamingService NAMES = new ContainerNamingService();

  private ContainerNameCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> nameNode() {
    return CommandManager.literal("name")
        .then(
            CommandManager.argument(CONTAINER, BlockPosArgumentType.blockPos())
                .executes(ContainerNameCommand::get)
                .then(
                    CommandManager.literal("set")
                        .then(
                            CommandManager.argument(NAME, StringArgumentType.greedyString())
                                .executes(ContainerNameCommand::set)))
                .then(CommandManager.literal("clear").executes(ContainerNameCommand::clear)));
  }

  private static int get(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos terminal = TerminalCommand.terminalPos(context);
    if (TerminalLookup.at(source.getWorld(), terminal).isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(terminal));
      return 0;
    }
    final BlockPos container = BlockPosArgumentType.getBlockPos(context, CONTAINER);
    final String name = NAMES.nameOf(source.getWorld(), terminal, container);
    source.sendFeedback(
        () ->
            name.isEmpty()
                ? ContainerMessages.containerUnnamed(container)
                : ContainerMessages.containerName(container, name),
        false);
    return name.isEmpty() ? 0 : 1;
  }

  private static int set(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final String requested = StringArgumentType.getString(context, NAME);
    if (!ContainerNameRule.isAcceptable(requested)) {
      context
          .getSource()
          .sendError(ContainerMessages.containerNameRefused(ContainerNameRule.MAX_LENGTH));
      return 0;
    }
    return apply(context, ContainerNameRule.sanitise(requested));
  }

  private static int clear(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    return apply(context, "");
  }

  private static int apply(CommandContext<ServerCommandSource> context, String name)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos terminal = TerminalCommand.terminalPos(context);
    final BlockPos container = BlockPosArgumentType.getBlockPos(context, CONTAINER);
    final RoleChangeOutcome outcome =
        NAMES.rename(
            source.getWorld(), terminal, TerminalCommandGate.caller(source), container, name);
    report(source, terminal, container, name, outcome);
    if (outcome.applied()) {
      TerminalViewerSync.refresh(
          source.getServer(), TerminalAddress.of(source.getWorld(), terminal));
    }
    return outcome.applied() ? 1 : 0;
  }

  private static void report(
      ServerCommandSource source,
      BlockPos terminal,
      BlockPos container,
      String name,
      RoleChangeOutcome outcome) {
    switch (outcome) {
      case CHANGED ->
          source.sendFeedback(
              () ->
                  name.isEmpty()
                      ? ContainerMessages.containerNameCleared(container)
                      : ContainerMessages.containerNameSet(container, name),
              true);
      case UNCHANGED ->
          source.sendFeedback(() -> ContainerMessages.containerNameUnchanged(container), false);
      case NOT_IN_NETWORK ->
          source.sendError(CommandMessages.containerNotReached(terminal, container));
      case NO_TERMINAL -> source.sendError(CommandMessages.noTerminalAt(terminal));
      case DENIED -> source.sendError(CommandMessages.terminalNotYours(terminal));
    }
  }
}
