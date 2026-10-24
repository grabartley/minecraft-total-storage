package com.grahambartley.totalstorage.config;

import java.util.List;
import java.util.Optional;

public final class ConfigSettings {
  private static final List<ConfigSetting> ALL =
      List.of(
          new BoundedIntConfigSetting(
              "maxTerminalRadius",
              TotalStorageConfig.MAX_TERMINAL_RADIUS_MIN,
              TotalStorageConfig.MAX_TERMINAL_RADIUS_MAX,
              TotalStorageConfig.DEFAULT_MAX_TERMINAL_RADIUS,
              TotalStorageConfig::maxTerminalRadius,
              ConfigBuilder::maxTerminalRadius),
          new EnumConfigSetting<>(
              "defaultTerminalAccessMode",
              TerminalAccessMode.class,
              TotalStorageConfig::defaultTerminalAccessMode,
              ConfigBuilder::defaultTerminalAccessMode,
              TotalStorageConfig.DEFAULT_TERMINAL_ACCESS_MODE),
          new EnumConfigSetting<>(
              "sortOrdering",
              SortOrdering.class,
              TotalStorageConfig::sortOrdering,
              ConfigBuilder::sortOrdering,
              TotalStorageConfig.DEFAULT_SORT_ORDERING),
          new BooleanConfigSetting(
              "sortButtonEnabled",
              TotalStorageConfig::sortButtonEnabled,
              ConfigBuilder::sortButtonEnabled,
              TotalStorageConfig.DEFAULT_SORT_BUTTON_ENABLED),
          new BoundedIntConfigSetting(
              "quickStackRadius",
              TotalStorageConfig.QUICK_STACK_RADIUS_MIN,
              TotalStorageConfig.QUICK_STACK_RADIUS_MAX,
              TotalStorageConfig.DEFAULT_QUICK_STACK_RADIUS,
              TotalStorageConfig::quickStackRadius,
              ConfigBuilder::quickStackRadius),
          new BooleanConfigSetting(
              "quickStackEnabled",
              TotalStorageConfig::quickStackEnabled,
              ConfigBuilder::quickStackEnabled,
              TotalStorageConfig.DEFAULT_QUICK_STACK_ENABLED),
          new BooleanConfigSetting(
              "depositAllSkipsHotbar",
              TotalStorageConfig::depositAllSkipsHotbar,
              ConfigBuilder::depositAllSkipsHotbar,
              TotalStorageConfig.DEFAULT_DEPOSIT_ALL_SKIPS_HOTBAR),
          new BoundedIntConfigSetting(
              "nearCapacityWarningPercent",
              TotalStorageConfig.NEAR_CAPACITY_WARNING_PERCENT_MIN,
              TotalStorageConfig.NEAR_CAPACITY_WARNING_PERCENT_MAX,
              TotalStorageConfig.DEFAULT_NEAR_CAPACITY_WARNING_PERCENT,
              TotalStorageConfig::nearCapacityWarningPercent,
              ConfigBuilder::nearCapacityWarningPercent,
              ConfigUnit.PERCENT),
          new BoundedIntConfigSetting(
              "expensiveOperationCooldownTicks",
              TotalStorageConfig.EXPENSIVE_OPERATION_COOLDOWN_TICKS_MIN,
              TotalStorageConfig.EXPENSIVE_OPERATION_COOLDOWN_TICKS_MAX,
              TotalStorageConfig.DEFAULT_EXPENSIVE_OPERATION_COOLDOWN_TICKS,
              TotalStorageConfig::expensiveOperationCooldownTicks,
              ConfigBuilder::expensiveOperationCooldownTicks),
          new BoundedIntConfigSetting(
              "movesPerTickBudget",
              TotalStorageConfig.MOVES_PER_TICK_BUDGET_MIN,
              TotalStorageConfig.MOVES_PER_TICK_BUDGET_MAX,
              TotalStorageConfig.DEFAULT_MOVES_PER_TICK_BUDGET,
              TotalStorageConfig::movesPerTickBudget,
              ConfigBuilder::movesPerTickBudget),
          new BoundedIntConfigSetting(
              "largeOperationContainers",
              TotalStorageConfig.LARGE_OPERATION_CONTAINERS_MIN,
              TotalStorageConfig.LARGE_OPERATION_CONTAINERS_MAX,
              TotalStorageConfig.DEFAULT_LARGE_OPERATION_CONTAINERS,
              TotalStorageConfig::largeOperationContainers,
              ConfigBuilder::largeOperationContainers));

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
