package com.grahambartley.toomanychests.command;

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

/**
 * The {@code /tmc terminal <pos>} subtree, where one terminal rather than the whole server is
 * configured.
 *
 * <p>The terminal is named by position rather than taken from what the caller is looking at,
 * because the console a server owner works from is looking at nothing. Reading is open, since a
 * radius is not a secret; moving one is gated like every other change this mod accepts.
 */
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
                                .requires(TooManyChestsCommand::isOperator)
                                .executes(TerminalCommand::setRadius))));
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
    return radius.get().blocks();
  }
}
