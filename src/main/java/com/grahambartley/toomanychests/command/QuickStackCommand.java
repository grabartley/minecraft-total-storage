package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.operation.QuickStackOutcome;
import com.grahambartley.toomanychests.operation.QuickStackService;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public final class QuickStackCommand {
  private static final QuickStackService QUICK_STACKS = new QuickStackService();

  private QuickStackCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("quickstack").executes(QuickStackCommand::quickStack);
  }

  private static int quickStack(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final QuickStackOutcome outcome = QUICK_STACKS.quickStack(source.getPlayerOrThrow());
    if (outcome.result().movedAnything()) {
      source.sendFeedback(
          () -> CommandMessages.quickStacked(outcome.moved(), outcome.containersTouched()), false);
      return (int) Math.min(Integer.MAX_VALUE, outcome.moved());
    }
    source.sendError(CommandMessages.nothingToQuickStack());
    return 0;
  }
}
