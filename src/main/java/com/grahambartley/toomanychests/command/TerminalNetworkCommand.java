package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.operation.ExpensiveOperation;
import com.grahambartley.toomanychests.operation.OperationGuards;
import com.grahambartley.toomanychests.screenhandler.TerminalViewerSync;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerHighlightService;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.RoleChangeOutcome;
import com.grahambartley.toomanychests.terminal.RoleTally;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalPower;
import com.grahambartley.toomanychests.terminal.TerminalRadius;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class TerminalNetworkCommand {
  private static final String CONTAINER = "container";
  private static final String ROLE = "role";

  private static final List<String> ROLE_NAMES =
      Arrays.stream(ContainerRole.values()).map(ContainerRole::id).toList();

  private static final SuggestionProvider<ServerCommandSource> ROLE_SUGGESTIONS =
      (context, builder) -> CommandSource.suggestMatching(ROLE_NAMES, builder);

  private static final ContainerRoleService ROLES = new ContainerRoleService();

  private TerminalNetworkCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> infoNode() {
    return CommandManager.literal("info").executes(TerminalNetworkCommand::info);
  }

  static LiteralArgumentBuilder<ServerCommandSource> roleNode() {
    return CommandManager.literal("role")
        .then(
            CommandManager.argument(CONTAINER, BlockPosArgumentType.blockPos())
                .executes(TerminalNetworkCommand::getRole)
                .then(
                    CommandManager.argument(ROLE, StringArgumentType.word())
                        .suggests(ROLE_SUGGESTIONS)
                        .executes(TerminalNetworkCommand::setRole)));
  }

  static LiteralArgumentBuilder<ServerCommandSource> rescanNode() {
    return CommandManager.literal("rescan").executes(TerminalNetworkCommand::rescan);
  }

  static LiteralArgumentBuilder<ServerCommandSource> highlightNode() {
    return CommandManager.literal("highlight").executes(TerminalNetworkCommand::highlight);
  }

  private static int info(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    final Optional<TerminalSettings> settings = TerminalLookup.settingsAt(source.getWorld(), pos);
    if (settings.isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
      return 0;
    }
    final TerminalRadius radius =
        TerminalRadius.of(
            settings.get().radius(), ServerConfigService.current().maxTerminalRadius());
    source.sendFeedback(() -> CommandMessages.terminalInfoHeader(pos), false);
    source.sendFeedback(() -> CommandMessages.terminalInfoRadius(radius), false);
    source.sendFeedback(
        () -> CommandMessages.terminalInfoAccess(settings.get().accessMode()), false);
    if (!TerminalCommandGate.caller(source).mayOpen(settings.get())) {
      return 1;
    }
    final boolean powered = TerminalNetworks.power().isActive(source.getWorld(), pos);
    final RoleTally tally = ROLES.tally(source.getWorld(), pos);
    source.sendFeedback(() -> CommandMessages.terminalInfoPower(powered), false);
    source.sendFeedback(() -> CommandMessages.terminalInfoContainers(tally), false);
    return tally.total();
  }

  private static int getRole(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    if (TerminalLookup.at(source.getWorld(), pos).isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
      return 0;
    }
    final BlockPos container = BlockPosArgumentType.getBlockPos(context, CONTAINER);
    final ContainerRole role = ROLES.roleOf(source.getWorld(), pos, container);
    source.sendFeedback(() -> CommandMessages.containerRole(container, role), false);
    return 1;
  }

  private static int setRole(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    final BlockPos container = BlockPosArgumentType.getBlockPos(context, CONTAINER);
    final String requested = StringArgumentType.getString(context, ROLE);
    final Optional<ContainerRole> role = ContainerRole.fromId(requested);
    if (role.isEmpty()) {
      source.sendError(CommandMessages.unknownRole(requested, String.join(", ", ROLE_NAMES)));
      return 0;
    }
    final RoleChangeOutcome outcome =
        ROLES.assign(
            source.getWorld(), pos, TerminalCommandGate.caller(source), container, role.get());
    report(source, pos, container, role.get(), outcome);
    if (outcome.applied()) {
      resyncViewers(source, pos);
    }
    return outcome.applied() ? 1 : 0;
  }

  private static int rescan(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    if (TerminalCommandGate.administrable(source, pos).isEmpty()) {
      return 0;
    }
    final TerminalPower power = TerminalNetworks.power();
    if (!power.isActive(source.getWorld(), pos)) {
      source.sendError(CommandMessages.terminalUnpowered(pos));
      return 0;
    }
    final Optional<Text> refusal =
        OperationGuards.refuse(source.getPlayer(), ExpensiveOperation.RESCAN);
    if (refusal.isPresent()) {
      source.sendError(refusal.get());
      return 0;
    }
    power.rescan(source.getWorld(), pos);
    final int held = power.containerCount(source.getWorld(), pos);
    source.sendFeedback(() -> CommandMessages.terminalRescanned(pos, held), true);
    resyncViewers(source, pos);
    return 1;
  }

  private static int highlight(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    final ServerPlayerEntity player = source.getPlayer();
    if (player == null) {
      source.sendError(CommandMessages.highlightNeedsAPlayer());
      return 0;
    }
    if (TerminalCommandGate.openable(source, pos).isEmpty()) {
      return 0;
    }
    final int shown = ContainerHighlightService.highlight(player, source.getWorld(), pos);
    source.sendFeedback(() -> CommandMessages.terminalHighlighted(pos, shown), false);
    return shown;
  }

  private static void report(
      ServerCommandSource source,
      BlockPos terminal,
      BlockPos container,
      ContainerRole role,
      RoleChangeOutcome outcome) {
    switch (outcome) {
      case CHANGED ->
          source.sendFeedback(() -> CommandMessages.containerRoleSet(container, role), true);
      case UNCHANGED ->
          source.sendFeedback(() -> CommandMessages.containerRoleUnchanged(container, role), false);
      case NOT_IN_NETWORK ->
          source.sendError(CommandMessages.containerNotReached(terminal, container));
      case NO_TERMINAL -> source.sendError(CommandMessages.noTerminalAt(terminal));
      case DENIED -> source.sendError(CommandMessages.terminalNotYours(terminal));
    }
  }

  private static void resyncViewers(ServerCommandSource source, BlockPos terminal) {
    TerminalViewerSync.refresh(source.getServer(), TerminalAddress.of(source.getWorld(), terminal));
  }
}
