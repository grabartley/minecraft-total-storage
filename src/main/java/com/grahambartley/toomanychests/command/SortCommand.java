package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.sort.ContainerSortService;
import com.grahambartley.toomanychests.sort.SortFeedback;
import com.grahambartley.toomanychests.sort.SortOutcome;
import com.grahambartley.toomanychests.sort.SortReply;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public final class SortCommand {
  private static final ContainerSortService SORTING = new ContainerSortService();

  private SortCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("sort").executes(SortCommand::sort);
  }

  private static int sort(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final ServerPlayerEntity player = source.getPlayerOrThrow();
    return report(source, SORTING.sortForCommand(player));
  }

  private static int report(ServerCommandSource source, SortReply reply) {
    final SortOutcome outcome = reply.outcome();
    final Text message = SortFeedback.messageFor(reply);
    if (outcome.wasRefused() || outcome == SortOutcome.FAILED) {
      source.sendError(message);
      return 0;
    }
    source.sendFeedback(() -> message, false);
    return outcome.wasSorted() ? 1 : 0;
  }
}
