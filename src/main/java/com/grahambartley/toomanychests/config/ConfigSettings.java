package com.grahambartley.toomanychests.config;

import java.util.List;
import java.util.Optional;

/**
 * Every server setting an operator can reach, in the order they are worth reading.
 *
 * <p>This is the one list. The command tree, its tab completion, and any settings screen all read
 * it, so a setting added to the config record and left out here is visibly absent from all three
 * rather than quietly missing from one.
 */
public final class ConfigSettings {
  private static final List<ConfigSetting> ALL =
      List.of(
          new BoundedIntConfigSetting(
              "maxTerminalRadius",
              TooManyChestsConfig.MAX_TERMINAL_RADIUS_MIN,
              TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX,
              TooManyChestsConfig.DEFAULT_MAX_TERMINAL_RADIUS,
              TooManyChestsConfig::maxTerminalRadius,
              ConfigBuilder::maxTerminalRadius),
          new EnumConfigSetting<>(
              "defaultTerminalAccessMode",
              TerminalAccessMode.class,
              TooManyChestsConfig::defaultTerminalAccessMode,
              ConfigBuilder::defaultTerminalAccessMode,
              TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE),
          new EnumConfigSetting<>(
              "sortOrdering",
              SortOrdering.class,
              TooManyChestsConfig::sortOrdering,
              ConfigBuilder::sortOrdering,
              TooManyChestsConfig.DEFAULT_SORT_ORDERING),
          new BooleanConfigSetting(
              "sortButtonEnabled",
              TooManyChestsConfig::sortButtonEnabled,
              ConfigBuilder::sortButtonEnabled,
              TooManyChestsConfig.DEFAULT_SORT_BUTTON_ENABLED),
          new BooleanConfigSetting(
              "middleClickSortEnabled",
              TooManyChestsConfig::middleClickSortEnabled,
              ConfigBuilder::middleClickSortEnabled,
              TooManyChestsConfig.DEFAULT_MIDDLE_CLICK_SORT_ENABLED),
          new BoundedIntConfigSetting(
              "quickStackRadius",
              TooManyChestsConfig.QUICK_STACK_RADIUS_MIN,
              TooManyChestsConfig.QUICK_STACK_RADIUS_MAX,
              TooManyChestsConfig.DEFAULT_QUICK_STACK_RADIUS,
              TooManyChestsConfig::quickStackRadius,
              ConfigBuilder::quickStackRadius),
          new BooleanConfigSetting(
              "depositAllSkipsHotbar",
              TooManyChestsConfig::depositAllSkipsHotbar,
              ConfigBuilder::depositAllSkipsHotbar,
              TooManyChestsConfig.DEFAULT_DEPOSIT_ALL_SKIPS_HOTBAR),
          new BoundedIntConfigSetting(
              "nearCapacityWarningPercent",
              TooManyChestsConfig.NEAR_CAPACITY_WARNING_PERCENT_MIN,
              TooManyChestsConfig.NEAR_CAPACITY_WARNING_PERCENT_MAX,
              TooManyChestsConfig.DEFAULT_NEAR_CAPACITY_WARNING_PERCENT,
              TooManyChestsConfig::nearCapacityWarningPercent,
              ConfigBuilder::nearCapacityWarningPercent),
          new BoundedIntConfigSetting(
              "expensiveOperationCooldownTicks",
              TooManyChestsConfig.EXPENSIVE_OPERATION_COOLDOWN_TICKS_MIN,
              TooManyChestsConfig.EXPENSIVE_OPERATION_COOLDOWN_TICKS_MAX,
              TooManyChestsConfig.DEFAULT_EXPENSIVE_OPERATION_COOLDOWN_TICKS,
              TooManyChestsConfig::expensiveOperationCooldownTicks,
              ConfigBuilder::expensiveOperationCooldownTicks),
          new BoundedIntConfigSetting(
              "movesPerTickBudget",
              TooManyChestsConfig.MOVES_PER_TICK_BUDGET_MIN,
              TooManyChestsConfig.MOVES_PER_TICK_BUDGET_MAX,
              TooManyChestsConfig.DEFAULT_MOVES_PER_TICK_BUDGET,
              TooManyChestsConfig::movesPerTickBudget,
              ConfigBuilder::movesPerTickBudget));

  private static final List<String> NAMES = ALL.stream().map(ConfigSetting::name).toList();

  private ConfigSettings() {}

  public static List<ConfigSetting> all() {
    return ALL;
  }

  public static List<String> names() {
    return NAMES;
  }

  public static Optional<ConfigSetting> byName(String name) {
    if (name == null) {
      return Optional.empty();
    }
    final String wanted = name.trim();
    return ALL.stream().filter(setting -> setting.name().equalsIgnoreCase(wanted)).findFirst();
  }
}
