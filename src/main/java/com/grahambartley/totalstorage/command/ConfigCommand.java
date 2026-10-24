package com.grahambartley.totalstorage.command;

import com.grahambartley.totalstorage.config.ConfigSetting;
import com.grahambartley.totalstorage.config.ConfigSettings;
import com.grahambartley.totalstorage.config.ServerConfigService;
import com.grahambartley.totalstorage.config.TotalStorageConfig;
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
                .then(
                    CommandManager.argument(SETTING, StringArgumentType.word())
                        .suggests(SETTING_NAMES)
                        .then(
                            CommandManager.argument(VALUE, StringArgumentType.word())
                                .suggests(SETTING_VALUES)
                                .executes(ConfigCommand::set))))
        .then(
            CommandManager.literal("reset")
                .then(CommandManager.literal("all").executes(ConfigCommand::resetAll))
                .then(
                    CommandManager.argument(SETTING, StringArgumentType.word())
                        .suggests(SETTING_NAMES)
                        .executes(ConfigCommand::reset)))
        .then(EligibilityCommand.node());
  }

  private static int list(CommandContext<ServerCommandSource> context) {
    final ServerCommandSource source = context.getSource();
    final TotalStorageConfig config = ServerConfigService.current();
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
    if (ConfigWriteGate.refuses(context)) {
      return 0;
    }
    final Optional<ConfigSetting> setting = named(context);
    if (setting.isEmpty()) {
      return unknown(context);
    }
    final String requested = StringArgumentType.getString(context, VALUE);
    final Optional<TotalStorageConfig> updated =
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
    if (ConfigWriteGate.refuses(context)) {
      return 0;
    }
    final Optional<ConfigSetting> setting = named(context);
    if (setting.isEmpty()) {
      return unknown(context);
    }
    final TotalStorageConfig updated = setting.get().reset(ServerConfigService.current());
    return apply(
        context,
        updated,
        CommandMessages.settingReset(setting.get(), setting.get().defaultValue()));
  }

  private static int resetAll(CommandContext<ServerCommandSource> context) {
    if (ConfigWriteGate.refuses(context)) {
      return 0;
    }
    return apply(context, TotalStorageConfig.defaults(), CommandMessages.everySettingReset());
  }

  static int apply(
      CommandContext<ServerCommandSource> context, TotalStorageConfig updated, Text success) {
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

  private static Optional<ConfigSetting> settingArgument(
      CommandContext<ServerCommandSource> context) {
    try {
      return ConfigSettings.byName(StringArgumentType.getString(context.getLastChild(), SETTING));
    } catch (IllegalArgumentException ex) {
      return Optional.empty();
    }
  }
}
