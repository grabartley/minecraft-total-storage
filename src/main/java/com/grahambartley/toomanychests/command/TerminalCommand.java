package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.terminal.ContainerHighlightService;
import com.grahambartley.toomanychests.terminal.TerminalNetworks;
import com.grahambartley.toomanychests.terminal.TerminalRadius;
import com.grahambartley.toomanychests.terminal.TerminalRadiusService;
import com.grahambartley.toomanychests.terminal.TerminalSettings;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Optional;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public final class TerminalCommand {
  private static final String POS = "pos";
  private static final String BLOCKS = "blocks";

  private static final TerminalRadiusService RADIUS =
      new TerminalRadiusService(TerminalNetworks.power());

  private TerminalCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("terminal")
        .then(
            CommandManager.argument(POS, BlockPosArgumentType.blockPos())
                .then(
                    CommandManager.literal("radius")
                        .executes(TerminalCommand::getRadius)
                        .then(
                            CommandManager.argument(
                                    BLOCKS,
                                    IntegerArgumentType.integer(
                                        TerminalSettings.MIN_RADIUS, TerminalSettings.MAX_RADIUS))
                                .executes(TerminalCommand::setRadius)))
                .then(TerminalAccessCommand.accessNode())
                .then(TerminalAccessCommand.trustNode())
                .then(TerminalAccessCommand.untrustNode())
                .then(TerminalNetworkCommand.infoNode())
                .then(TerminalNetworkCommand.roleNode())
                .then(ContainerNameCommand.nameNode())
                .then(ContainerFilterCommand.filterNode())
                .then(TerminalNetworkCommand.rescanNode())
                .then(TerminalNetworkCommand.highlightNode())
                .then(TerminalTagCommand.tagsNode()));
  }

  static BlockPos terminalPos(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    return BlockPosArgumentType.getLoadedBlockPos(context, POS);
  }

  private static int getRadius(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, POS);
    final Optional<TerminalRadius> radius = RADIUS.current(source.getWorld(), pos);
    if (radius.isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
      return 0;
    }
    final Text reply =
        radius.get().clamped()
            ? CommandMessages.terminalRadiusHeldDown(pos, radius.get())
            : CommandMessages.terminalRadius(pos, radius.get());
    source.sendFeedback(() -> reply, false);
    return radius.get().blocks();
  }

  private static int setRadius(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = BlockPosArgumentType.getLoadedBlockPos(context, POS);
    if (TerminalCommandGate.administrable(source, pos).isEmpty()) {
      return 0;
    }
    final Optional<TerminalRadius> radius =
        RADIUS.apply(source.getWorld(), pos, IntegerArgumentType.getInteger(context, BLOCKS));
    if (radius.isEmpty()) {
      source.sendError(CommandMessages.noTerminalAt(pos));
      return 0;
    }
    final Text reply =
        radius.get().clamped()
            ? CommandMessages.terminalRadiusClamped(pos, radius.get())
            : CommandMessages.terminalRadiusSet(pos, radius.get());
    source.sendFeedback(() -> reply, true);
    if (source.getPlayer() != null) {
      ContainerHighlightService.highlight(source.getPlayer(), source.getWorld(), pos);
    }
    return radius.get().blocks();
  }
}
