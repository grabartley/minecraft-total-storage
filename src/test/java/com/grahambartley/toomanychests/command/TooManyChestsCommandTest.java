package com.grahambartley.toomanychests.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.api.Test;

class TooManyChestsCommandTest {
  @Test
  void everyTerminalSubcommandHasExactlyTheHelpLinesThatNameIt() {
    assertEquals(registeredTerminalSubcommands(), subcommandsWithHelp());
  }

  private static Set<String> registeredTerminalSubcommands() {
    CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
    TooManyChestsCommand.register(dispatcher);
    CommandNode<ServerCommandSource> terminal =
        dispatcher.getRoot().getChild("toomanychests").getChild("terminal");
    CommandNode<ServerCommandSource> position = terminal.getChildren().iterator().next();
    return position.getChildren().stream()
        .filter(LiteralCommandNode.class::isInstance)
        .map(CommandNode::getName)
        .collect(Collectors.toSet());
  }

  private static Set<String> subcommandsWithHelp() {
    return TooManyChestsCommand.helpLines().stream()
        .flatMap(line -> line.terminalSubcommands().stream())
        .collect(Collectors.toSet());
  }
}
