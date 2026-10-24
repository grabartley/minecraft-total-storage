package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.tag.NetworkTagSet;
import com.grahambartley.totalstorage.tag.TagEditOutcome;
import com.grahambartley.totalstorage.tag.TagSetContents;
import com.grahambartley.totalstorage.tag.TagSetFile;
import com.grahambartley.totalstorage.tag.TagSetFileName;
import com.grahambartley.totalstorage.tag.TagSetImportMode;
import com.grahambartley.totalstorage.tag.TagTransferResult;
import com.grahambartley.totalstorage.terminal.NetworkTagTransfer;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.nio.file.Path;
import java.util.Optional;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
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

  private static RequiredArgumentBuilder<ServerCommandSource, String> modes(
      Command<ServerCommandSource> run) {
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
      source.sendError(refusalAt(source, pos));
      return 0;
    }
    if (!TagSetFile.save(path.get(), tags.get())) {
      source.sendError(TagSetMessages.exportFailed());
      return 0;
    }
    final int exported = tags.get().custom().size();
    source.sendFeedback(() -> TagSetMessages.exported(exported, fileName(context)), true);
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
    final Optional<TagSetContents> loaded = TagSetFile.load(path.get());
    if (loaded.isEmpty()) {
      source.sendError(TagSetMessages.fileUnreadable(fileName(context)));
      return 0;
    }
    final BlockPos terminal = TerminalCommand.terminalPos(context);
    return report(
        source,
        terminal,
        TRANSFER.importInto(
            source.getWorld(),
            terminal,
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
    final BlockPos terminal = TerminalCommand.terminalPos(context);
    return report(
        source,
        terminal,
        TRANSFER.copy(
            source.getWorld(),
            terminal,
            BlockPosArgumentType.getLoadedBlockPos(context, SOURCE),
            TerminalCommandGate.caller(source),
            mode.get()));
  }

  private static int report(
      ServerCommandSource source, BlockPos terminal, TagTransferResult result) {
    return switch (result.outcome()) {
      case CHANGED -> {
        source.sendFeedback(() -> TagSetMessages.imported(result.imported(), result.skips()), true);
        yield result.imported();
      }
      case UNCHANGED -> {
        source.sendFeedback(() -> TagSetMessages.importedNothing(result.skips()), false);
        yield 0;
      }
      case NO_TERMINAL -> {
        source.sendError(CommandMessages.noTerminalAt(terminal));
        yield 0;
      }
      default -> {
        source.sendError(TagSetMessages.transferRefused());
        yield 0;
      }
    };
  }

  private static Text refusalAt(ServerCommandSource source, BlockPos pos) {
    return NetworkTagTransfer.refusalFor(source.getWorld(), pos) == TagEditOutcome.NO_TERMINAL
        ? CommandMessages.noTerminalAt(pos)
        : CommandMessages.terminalNotYours(pos);
  }

  private static String fileName(CommandContext<ServerCommandSource> context) {
    return StringArgumentType.getString(context, FILE);
  }

  private static Optional<Path> pathFor(CommandContext<ServerCommandSource> context) {
    final ServerCommandSource source = context.getSource();
    final String raw = fileName(context);
    final Optional<String> name = TagSetFileName.of(raw);
    if (name.isEmpty()) {
      source.sendError(TagSetMessages.fileNameRejected(raw));
      return Optional.empty();
    }
    return Optional.of(ServerConfigService.pathsFor(source.getServer()).getTagSetPath(name.get()));
  }

  private static Optional<TagSetImportMode> modeFor(CommandContext<ServerCommandSource> context) {
    final String raw = StringArgumentType.getString(context, MODE);
    final Optional<TagSetImportMode> mode = TagSetImportMode.fromId(raw);
    if (mode.isEmpty()) {
      context.getSource().sendError(TagSetMessages.unknownMode(raw));
    }
    return mode;
  }
}
