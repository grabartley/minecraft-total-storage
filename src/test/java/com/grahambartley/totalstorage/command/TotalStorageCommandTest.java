package com.grahambartley.totalstorage.command;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.command.ServerCommandSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class TotalStorageCommandTest {
  private static final CommandNode<ServerCommandSource> ROOT = registeredRoot();

  @ParameterizedTest
  @MethodSource("registeredCommands")
  void everyRegisteredCommandHasAHelpLine(String command) {
    assertTrue(
        documented().stream()
            .anyMatch(line -> line.equals(command) || line.startsWith(command + " ")),
        "/ts " + command + " has no help line");
  }

  @ParameterizedTest
  @MethodSource("documentedCommands")
  void everyHelpLineNamesACommandThatIsRegistered(String command) {
    assertTrue(resolve(command).isPresent(), "The help names /ts " + command + ", which is gone");
  }

  private static Stream<String> registeredCommands() {
    List<String> commands = new ArrayList<>();
    for (CommandNode<ServerCommandSource> top : literalsUnder(ROOT)) {
      commands.add(top.getName());
    }
    CommandNode<ServerCommandSource> terminal = ROOT.getChild("terminal");
    for (CommandNode<ServerCommandSource> sub : literalsUnder(terminal)) {
      commands.add("terminal " + sub.getName());
    }
    return commands.stream();
  }

  private static Stream<String> documentedCommands() {
    return documented().stream();
  }

  private static Set<String> documented() {
    return TotalStorageCommand.helpLines().stream()
        .flatMap(line -> line.commands().stream())
        .collect(Collectors.toSet());
  }

  private static Optional<CommandNode<ServerCommandSource>> resolve(String command) {
    CommandNode<ServerCommandSource> node = ROOT;
    for (String word : command.split(" ")) {
      node =
          literalsUnder(node).stream()
              .filter(child -> child.getName().equals(word))
              .findFirst()
              .orElse(null);
      if (node == null) {
        return Optional.empty();
      }
    }
    return Optional.of(node);
  }

  private static List<CommandNode<ServerCommandSource>> literalsUnder(
      CommandNode<ServerCommandSource> node) {
    List<CommandNode<ServerCommandSource>> literals = new ArrayList<>();
    for (CommandNode<ServerCommandSource> child : node.getChildren()) {
      if (child instanceof LiteralCommandNode) {
        literals.add(child);
      } else if (child instanceof ArgumentCommandNode) {
        child.getChildren().stream()
            .filter(LiteralCommandNode.class::isInstance)
            .forEach(literals::add);
      }
    }
    return literals;
  }

  private static CommandNode<ServerCommandSource> registeredRoot() {
    CommandDispatcher<ServerCommandSource> dispatcher = new CommandDispatcher<>();
    TotalStorageCommand.register(dispatcher);
    return dispatcher.getRoot().getChild("totalstorage");
  }
}
