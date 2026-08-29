package com.grahambartley.toomanychests.command;

import com.grahambartley.toomanychests.config.ConfigSetting;
import com.grahambartley.toomanychests.config.ConfigSettings;
import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.config.TooManyChestsConfig;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import java.util.Optional;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

/**
 * The {@code /tmc config} subtree, driven by the settings list rather than a node per setting.
 *
 * <p>Reading is open to anyone, since a setting a client is already told about on join is not a
 * secret. Changing one is not: every mutating branch is gated, and the gate is on the branch rather
 * than inside the handler so a player without it never sees the subcommand offered.
 */
public final class ConfigCommand {
  private static final String SETTING = "setting";
  private static final String VALUE = "value";

  private static final SuggestionProvider<ServerCommandSource> SETTING_NAMES =
      (context, builder) -> CommandSource.suggestMatching(ConfigSettings.names(), builder);

  private static final SuggestionProvider<ServerCommandSource> SETTING_VALUES =
      (context, builder) ->
          CommandSource.suggestMatching(
              settingArgument(context).map(ConfigSetting::suggestions).orElse(List.of()), builder);

  private ConfigCommand() {}

  static LiteralArgumentBuilder<ServerCommandSource> node() {
    return CommandManager.literal("config")
        .executes(ConfigCommand::list)
        .then(CommandManager.literal("list").executes(ConfigCommand::list))
        .then(
            CommandManager.literal("get")
                .then(
                    CommandManager.argument(SETTING, StringArgumentType.word())
                        .suggests(SETTING_NAMES)
                        .executes(ConfigCommand::get)))
        .then(
            CommandManager.literal("set")
                .requires(TooManyChestsCommand::isOperator)
                .then(
                    CommandManager.argument(SETTING, StringArgumentType.word())
                        .suggests(SETTING_NAMES)
                        .then(
                            CommandManager.argument(VALUE, StringArgumentType.word())
                                .suggests(SETTING_VALUES)
                                .executes(ConfigCommand::set))))
        .then(
            CommandManager.literal("reset")
                .requires(TooManyChestsCommand::isOperator)
                .then(CommandManager.literal("all").executes(ConfigCommand::resetAll))
                .then(
                    CommandManager.argument(SETTING, StringArgumentType.word())
                        .suggests(SETTING_NAMES)
                        .executes(ConfigCommand::reset)))
        .then(EligibilityCommand.node());
  }

  private static int list(CommandContext<ServerCommandSource> context) {
    final ServerCommandSource source = context.getSource();
    final TooManyChestsConfig config = ServerConfigService.current();
    source.sendFeedback(CommandMessages::configHeader, false);
    for (final ConfigSetting setting : ConfigSettings.all()) {
      source.sendFeedback(() -> CommandMessages.settingLine(setting, config), false);
    }
    return ConfigSettings.all().size();
  }

  private static int get(CommandContext<ServerCommandSource> context) {
    final ServerCommandSource source = context.getSource();
    final Optional<ConfigSetting> setting = named(context);
    if (setting.isEmpty()) {
      return unknown(context);
    }
    source.sendFeedback(
        () -> CommandMessages.settingValue(setting.get(), ServerConfigService.current()), false);
    return 1;
  }

  private static int set(CommandContext<ServerCommandSource> context) {
    final Optional<ConfigSetting> setting = named(context);
    if (setting.isEmpty()) {
      return unknown(context);
    }
    final String requested = StringArgumentType.getString(context, VALUE);
    final Optional<TooManyChestsConfig> updated =
        setting.get().withValue(ServerConfigService.current(), requested);
    if (updated.isEmpty()) {
      context.getSource().sendError(CommandMessages.invalidValue(setting.get(), requested));
      return 0;
    }
    return apply(
        context,
        updated.get(),
        CommandMessages.settingSet(setting.get(), setting.get().valueIn(updated.get())));
  }

  private static int reset(CommandContext<ServerCommandSource> context) {
    final Optional<ConfigSetting> setting = named(context);
    if (setting.isEmpty()) {
      return unknown(context);
    }
    final TooManyChestsConfig updated = setting.get().reset(ServerConfigService.current());
    return apply(
        context,
        updated,
        CommandMessages.settingReset(setting.get(), setting.get().defaultValue()));
  }

  private static int resetAll(CommandContext<ServerCommandSource> context) {
    return apply(context, TooManyChestsConfig.defaults(), CommandMessages.everySettingReset());
  }

  /**
   * Saves and announces a change, or says why it did not happen.
   *
   * <p>Broadcast to operators, because a setting moving is a server-wide event and the operator who
   * did not type it still has to know it moved.
   */
  static int apply(
      CommandContext<ServerCommandSource> context, TooManyChestsConfig updated, Text success) {
    final ServerCommandSource source = context.getSource();
    if (!ServerConfigService.update(source.getServer(), updated)) {
      source.sendError(CommandMessages.updateFailed());
      return 0;
    }
    source.sendFeedback(() -> success, true);
    return 1;
  }

  private static Optional<ConfigSetting> named(CommandContext<ServerCommandSource> context) {
    return ConfigSettings.byName(StringArgumentType.getString(context, SETTING));
  }

  private static int unknown(CommandContext<ServerCommandSource> context) {
    context
        .getSource()
        .sendError(
            CommandMessages.unknownSetting(
                StringArgumentType.getString(context, SETTING),
                String.join(", ", ConfigSettings.names())));
    return 0;
  }

  /**
   * The setting already named on the command line, while the value after it is being suggested.
   *
   * <p>Read from the last child rather than the context handed in, because a command reached
   * through the {@code /tmc} alias parses into a redirected child context and the outer one a
   * suggestion provider is given carries no arguments at all.
   */
  private static Optional<ConfigSetting> settingArgument(
      CommandContext<ServerCommandSource> context) {
    try {
      return ConfigSettings.byName(StringArgumentType.getString(context.getLastChild(), SETTING));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
