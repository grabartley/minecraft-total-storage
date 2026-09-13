package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import com.grahambartley.toomanychests.tag.TagSetFile;
import com.grahambartley.toomanychests.tag.TagSetFileName;
import com.grahambartley.toomanychests.tag.TagSetImportMode;
import com.grahambartley.toomanychests.tag.TagTransferResult;
import com.grahambartley.toomanychests.terminal.NetworkTagTransfer;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public final class TerminalTagCommand {
  private static final String FILE = "file";
  private static final String MODE = "mode";
  private static final String SOURCE = "from";

  private static final NetworkTagTransfer TRANSFER = new NetworkTagTransfer();

  private TerminalTagCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> tagsNode() {
    return CommandManager.literal("tags")
        .then(
            CommandManager.literal("export")
                .then(
                    CommandManager.argument(FILE, StringArgumentType.word())
                        .executes(TerminalTagCommand::export)))
        .then(
            CommandManager.literal("import")
                .then(
                    CommandManager.argument(FILE, StringArgumentType.word())
                        .then(modes(TerminalTagCommand::importFile))))
        .then(
            CommandManager.literal("copy")
                .then(
                    CommandManager.argument(SOURCE, BlockPosArgumentType.blockPos())
                        .then(modes(TerminalTagCommand::copy))));
  }

  private static com.mojang.brigadier.builder.RequiredArgumentBuilder<ServerCommandSource, String>
      modes(com.mojang.brigadier.Command<ServerCommandSource> run) {
    return CommandManager.argument(MODE, StringArgumentType.word())
        .suggests(
            (context, builder) -> {
              for (final TagSetImportMode mode : TagSetImportMode.values()) {
                builder.suggest(mode.id());
              }
              return builder.buildFuture();
            })
        .executes(run);
  }

  private static int export(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final BlockPos pos = TerminalCommand.terminalPos(context);
    final Optional<Path> path = pathFor(context);
    if (path.isEmpty()) {
      return 0;
    }
    final Optional<NetworkTagSet> tags =
        TRANSFER.read(source.getWorld(), pos, TerminalCommandGate.caller(source));
    if (tags.isEmpty()) {
      source.sendError(CommandMessages.terminalNotYours(pos));
      return 0;
    }
    if (!TagSetFile.save(path.get(), tags.get())) {
      source.sendError(CommandMessages.tagSetExportFailed());
      return 0;
    }
    final int exported = tags.get().custom().size();
    source.sendFeedback(() -> CommandMessages.tagSetExported(exported, fileName(context)), true);
    return exported;
  }

  private static int importFile(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final Optional<Path> path = pathFor(context);
    final Optional<TagSetImportMode> mode = modeFor(context);
    if (path.isEmpty() || mode.isEmpty()) {
      return 0;
    }
    final Optional<NetworkTagSet> loaded = TagSetFile.load(path.get());
    if (loaded.isEmpty()) {
      source.sendError(CommandMessages.tagSetFileUnreadable(fileName(context)));
      return 0;
    }
    return report(
        source,
        TRANSFER.importInto(
            source.getWorld(),
            TerminalCommand.terminalPos(context),
            TerminalCommandGate.caller(source),
            loaded.get(),
            mode.get()));
  }

  private static int copy(CommandContext<ServerCommandSource> context)
      throws CommandSyntaxException {
    final ServerCommandSource source = context.getSource();
    final Optional<TagSetImportMode> mode = modeFor(context);
    if (mode.isEmpty()) {
      return 0;
    }
    return report(
        source,
        TRANSFER.copy(
            source.getWorld(),
            TerminalCommand.terminalPos(context),
            BlockPosArgumentType.getLoadedBlockPos(context, SOURCE),
            TerminalCommandGate.caller(source),
            mode.get()));
  }

  private static int report(ServerCommandSource source, TagTransferResult result) {
    return switch (result.outcome()) {
      case CHANGED -> {
        source.sendFeedback(
            () -> CommandMessages.tagSetImported(result.imported(), result.skipped()), true);
        yield result.imported();
      }
      case UNCHANGED -> {
        source.sendFeedback(() -> CommandMessages.tagSetImportedNothing(result.skipped()), false);
        yield 0;
      }
      default -> {
        source.sendError(CommandMessages.tagSetTransferRefused());
        yield 0;
      }
    };
  }

  private static String fileName(CommandContext<ServerCommandSource> context) {
    return StringArgumentType.getString(context, FILE);
  }

  private static Optional<Path> pathFor(CommandContext<ServerCommandSource> context) {
    final ServerCommandSource source = context.getSource();
    final String raw = fileName(context);
    final Optional<String> name = TagSetFileName.of(raw);
    if (name.isEmpty()) {
      source.sendError(CommandMessages.tagSetFileNameRejected(raw));
      return Optional.empty();
    }
    return Optional.of(ServerConfigService.pathsFor(source.getServer()).getTagSetPath(name.get()));
  }

  private static Optional<TagSetImportMode> modeFor(CommandContext<ServerCommandSource> context) {
    final String raw = StringArgumentType.getString(context, MODE);
    final Optional<TagSetImportMode> mode = TagSetImportMode.fromId(raw);
    if (mode.isEmpty()) {
      context.getSource().sendError(CommandMessages.unknownTagSetMode(raw));
    }
    return mode;
  }
}
