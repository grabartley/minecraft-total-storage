package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.access.TerminalCaller;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.network.ModNetworking;
import com.grahambartley.toomanychests.network.TerminalSettingsNetworking;
import com.grahambartley.toomanychests.screenhandler.TerminalViewerSync;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.terminal.ContainerHighlightService;
import com.grahambartley.toomanychests.terminal.ContainerRoleService;
import com.grahambartley.toomanychests.terminal.RoleChangeOutcome;
import com.grahambartley.toomanychests.terminal.RoleTally;
import com.grahambartley.toomanychests.terminal.TerminalAddress;
import com.grahambartley.toomanychests.terminal.TerminalLookup;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
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
import net.minecraft.util.math.BlockPos;

/**
 * The branches of {@code /tmc terminal <pos>} that reach a network's containers rather than its
 * access list: what it is holding, what it may do with each of them, and asking it to look again.
 *
 * <p>These exist so a container role is reachable without a client mod at all, which the mod's
 * standards require of every setting. Until they did, {@code EXCLUDED} was a role the model could
 * hold and nothing could give, which made ADR 0004's whole promise unavailable to a player.
 *
 * <p>Gated inside each handler rather than with {@code requires}, for the reason {@link
 * TerminalAccessCommand} explains: who may run one depends on the terminal the position names, and
 * Brigadier settles {@code requires} before it has parsed a position.
 */
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

  /** Open to anyone: what a terminal reaches is what a player standing at it can already see. */
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
    final boolean powered = TerminalNetworks.power().isActive(source.getWorld(), pos);
    final RoleTally tally = ROLES.tally(source.getWorld(), pos);
    source.sendFeedback(() -> CommandMessages.terminalInfoHeader(pos), false);
    source.sendFeedback(() -> CommandMessages.terminalInfoPower(powered), false);
    source.sendFeedback(() -> CommandMessages.terminalInfoRadius(radius), false);
    source.sendFeedback(
        () -> CommandMessages.terminalInfoAccess(settings.get().accessMode()), false);
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
            source.getWorld(),
            pos,
            TerminalCaller.of(source.getPlayer(), TooManyChestsCommand.isOperator(source)),
            container,
            role.get());
    report(source, pos, container, role.get(), outcome);
    if (outcome.applied()) {
      resyncViewers(source, pos);
    }
    return outcome.applied() ? 1 : 0;
  }

  /** Gated like every other verb that makes a terminal do work, and for the same reason. */
  private static int rescan(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    final Optional<TerminalSettings> settings = TerminalLookup.settingsAt(source.getWorld(), pos);
    if (settings.isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
      return 0;
    }
    if (!TerminalCaller.of(source.getPlayer(), TooManyChestsCommand.isOperator(source))
        .mayAdminister(settings.get())) {
      source.sendError(CommandMessages.terminalNotYours(pos));
      return 0;
    }
    final boolean rescanned = TerminalNetworks.power().rescan(source.getWorld(), pos);
    source.sendFeedback(() -> CommandMessages.terminalRescanned(pos, rescanned), rescanned);
    resyncViewers(source, pos);
    return rescanned ? 1 : 0;
  }

  /** Needs a player, because an outline drawn in a world nobody is looking at reaches nobody. */
  private static int highlight(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    final ServerPlayerEntity player = source.getPlayer();
    if (player == null) {
      source.sendError(CommandMessages.highlightNeedsAPlayer());
      return 0;
    }
    if (TerminalLookup.at(source.getWorld(), pos).isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
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

  /** A role changed from the console still has to reach whoever is looking at that terminal. */
  private static void resyncViewers(ServerCommandSource source, BlockPos terminal) {
    TerminalViewerSync.playersOn(
            source.getServer(), TerminalAddress.of(source.getWorld(), terminal))
        .forEach(
            viewer -> {
              TerminalSettingsNetworking.panels().refresh(viewer);
              ModNetworking.listings().refresh(viewer);
            });
  }
}
