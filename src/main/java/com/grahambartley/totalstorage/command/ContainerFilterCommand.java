package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.filter.FilterText;
import com.grahambartley.totalstorage.screenhandler.TerminalViewerSync;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.tag.NetworkTag;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.terminal.ContainerFilterService;
import com.grahambartley.totalstorage.terminal.ContainerRoleService;
import com.grahambartley.totalstorage.terminal.RoleChangeOutcome;
import com.grahambartley.totalstorage.terminal.TerminalAddress;
import com.grahambartley.totalstorage.terminal.TerminalLookup;
import com.grahambartley.totalstorage.terminal.TerminalSettings;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public final class ContainerFilterCommand {
  private static final String CONTAINER = "container";
  private static final String TAG = "tag";

  private static final ContainerFilterService FILTERS = new ContainerFilterService();
  private static final ContainerRoleService ROLES = new ContainerRoleService();

  private static final SuggestionProvider<ServerCommandSource> TAG_SUGGESTIONS =
      (context, builder) ->
          CommandSource.suggestMatching(
              TerminalLookup.settingsAt(
                      context.getSource().getWorld(), TerminalCommand.terminalPos(context))
                  .map(settings -> settings.tags().all().stream().map(NetworkTag::id).toList())
                  .orElse(List.of()),
              builder);

  private ContainerFilterCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> filterNode() {
    return CommandManager.literal("filter")
        .then(
            CommandManager.argument(CONTAINER, BlockPosArgumentType.blockPos())
                .executes(ContainerFilterCommand::list)
                .then(
                    CommandManager.literal("add")
                        .then(
                            CommandManager.argument(TAG, StringArgumentType.word())
                                .suggests(TAG_SUGGESTIONS)
                                .executes(context -> accept(context, true))))
                .then(
                    CommandManager.literal("remove")
                        .then(
                            CommandManager.argument(TAG, StringArgumentType.word())
                                .suggests(TAG_SUGGESTIONS)
                                .executes(context -> accept(context, false))))
                .then(CommandManager.literal("clear").executes(ContainerFilterCommand::clear)));
  }

  private static int list(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos terminal = TerminalCommand.terminalPos(context);
    if (TerminalLookup.at(source.getWorld(), terminal).isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(terminal));
      return 0;
    }
    final BlockPos container = BlockPosArgumentType.getBlockPos(context, CONTAINER);
    if (!isFiltered(source, terminal, container)) {
      source.sendFeedback(() -> ContainerMessages.containerNotFiltered(container), false);
      return 0;
    }
    final ContainerFilter filter =
        ContainerFilterService.onlyKnown(
            FILTERS.filterOf(source.getWorld(), terminal, container), tagsOf(source, terminal));
    source.sendFeedback(
        () ->
            filter.tagIds().isEmpty()
                ? ContainerMessages.containerFilterEmpty(container)
                : ContainerMessages.containerFilter(container, filter.tagIds()),
        false);
    return filter.tagIds().size();
  }

  private static int accept(CommandContext<ServerCommandSource> context, boolean accepted)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos terminal = TerminalCommand.terminalPos(context);
    final BlockPos container = BlockPosArgumentType.getBlockPos(context, CONTAINER);
    final String tagId = FilterText.tagId(StringArgumentType.getString(context, TAG));
    if (!hasTerminal(source, terminal)) {
      return 0;
    }
    if (!isFiltered(source, terminal, container)) {
      source.sendError(ContainerMessages.containerNotFiltered(container));
      return 0;
    }
    if (!knowsTag(source, terminal, tagId)) {
      source.sendError(ContainerMessages.unknownTag(tagId));
      return 0;
    }
    final RoleChangeOutcome outcome =
        FILTERS.accept(
            source.getWorld(),
            terminal,
            TerminalCommandGate.caller(source),
            container,
            tagId,
            accepted);
    switch (outcome) {
      case CHANGED ->
          source.sendFeedback(
              () ->
                  accepted
                      ? ContainerMessages.containerFilterAdded(container, tagId)
                      : ContainerMessages.containerFilterRemoved(container, tagId),
              true);
      case UNCHANGED ->
          source.sendFeedback(
              () -> ContainerMessages.containerFilterUnchanged(container, tagId), false);
      case NOT_IN_NETWORK ->
          source.sendError(CommandMessages.containerNotReached(terminal, container));
      case NO_TERMINAL -> source.sendError(CommandMessages.noTerminalAt(terminal));
      case DENIED -> source.sendError(CommandMessages.terminalNotYours(terminal));
    }
    if (outcome.applied()) {
      resync(source, terminal);
    }
    return outcome.applied() ? 1 : 0;
  }

  private static int clear(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos terminal = TerminalCommand.terminalPos(context);
    final BlockPos container = BlockPosArgumentType.getBlockPos(context, CONTAINER);
    if (!hasTerminal(source, terminal)) {
      return 0;
    }
    if (!isFiltered(source, terminal, container)) {
      source.sendError(ContainerMessages.containerNotFiltered(container));
      return 0;
    }
    final RoleChangeOutcome outcome =
        FILTERS.clear(source.getWorld(), terminal, TerminalCommandGate.caller(source), container);
    switch (outcome) {
      case CHANGED, UNCHANGED ->
          source.sendFeedback(
              () -> ContainerMessages.containerFilterCleared(container), outcome.applied());
      case NOT_IN_NETWORK ->
          source.sendError(CommandMessages.containerNotReached(terminal, container));
      case NO_TERMINAL -> source.sendError(CommandMessages.noTerminalAt(terminal));
      case DENIED -> source.sendError(CommandMessages.terminalNotYours(terminal));
    }
    if (outcome.applied()) {
      resync(source, terminal);
    }
    return outcome.applied() ? 1 : 0;
  }

  private static NetworkTagSet tagsOf(ServerCommandSource source, BlockPos terminal) {
    return TerminalLookup.settingsAt(source.getWorld(), terminal)
        .map(TerminalSettings::tags)
        .orElseGet(NetworkTagSet::defaults);
  }

  private static boolean isFiltered(
      ServerCommandSource source, BlockPos terminal, BlockPos container) {
    return TerminalLookup.at(source.getWorld(), terminal).isPresent()
        && ROLES.roleOf(source.getWorld(), terminal, container) == ContainerRole.FILTERED;
  }

  private static boolean hasTerminal(ServerCommandSource source, BlockPos terminal) {
    if (TerminalLookup.at(source.getWorld(), terminal).isPresent()) {
      return true;
    }
    source.sendError(CommandMessages.noTerminalAt(terminal));
    return false;
  }

  private static boolean knowsTag(ServerCommandSource source, BlockPos terminal, String tagId) {
    return TerminalLookup.settingsAt(source.getWorld(), terminal)
        .map(settings -> settings.hasTag(tagId))
        .orElse(false);
  }

  private static void resync(ServerCommandSource source, BlockPos terminal) {
    TerminalViewerSync.refresh(source.getServer(), TerminalAddress.of(source.getWorld(), terminal));
  }
}
