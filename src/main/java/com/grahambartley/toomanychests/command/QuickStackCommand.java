package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.network.ModNetworking;
import com.grahambartley.toomanychests.operation.NetworkOperationMessages;
import com.grahambartley.toomanychests.operation.QuickStackOutcome;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class QuickStackCommand {
  private QuickStackCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("quickstack").executes(QuickStackCommand::quickStack);
  }

  private static int quickStack(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final QuickStackOutcome outcome =
        ModNetworking.quickStacks().quickStack(source.getPlayerOrThrow());
    return switch (outcome.result()) {
      case STACKED -> stacked(source, outcome);
      case NOTHING_TO_STACK -> refuse(source, NetworkOperationMessages.nothingToQuickStack());
      case RATE_LIMITED -> 0;
      case REFUSED -> refuse(source, CommandMessages.quickStackDisabled());
    };
  }

  private static int stacked(ServerCommandSource source, QuickStackOutcome outcome) {
    source.sendFeedback(
        () -> NetworkOperationMessages.quickStacked(outcome.moved(), outcome.containersTouched()),
        false);
    return (int) Math.min(Integer.MAX_VALUE, outcome.moved());
  }

  private static int refuse(ServerCommandSource source, Text reason) {
    source.sendError(reason);
    return 0;
  }
}
