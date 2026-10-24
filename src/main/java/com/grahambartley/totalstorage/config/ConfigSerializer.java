package com.grahambartley.totalstorage.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfigSerializer {
  static final String KEY_MAX_TERMINAL_RADIUS = "maxTerminalRadius";
  static final String KEY_DEFAULT_TERMINAL_ACCESS_MODE = "defaultTerminalAccessMode";
  static final String KEY_SORT_ORDERING = "sortOrdering";
  static final String KEY_SORT_BUTTON_ENABLED = "sortButtonEnabled";
  static final String KEY_QUICK_STACK_RADIUS = "quickStackRadius";
  static final String KEY_QUICK_STACK_ENABLED = "quickStackEnabled";
  static final String KEY_DEPOSIT_ALL_SKIPS_HOTBAR = "depositAllSkipsHotbar";
  static final String KEY_NEAR_CAPACITY_WARNING_PERCENT = "nearCapacityWarningPercent";
  static final String KEY_EXPENSIVE_OPERATION_COOLDOWN_TICKS = "expensiveOperationCooldownTicks";
  static final String KEY_MOVES_PER_TICK_BUDGET = "movesPerTickBudget";
  static final String KEY_LARGE_OPERATION_CONTAINERS = "largeOperationContainers";
  static final String KEY_CONTAINER_ELIGIBILITY_OVERRIDES = "containerEligibilityOverrides";

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSerializer.class);

  private ConfigSerializer() {}

  public static JsonObject toJson(TotalStorageConfig config) {
    final JsonObject root = new JsonObject();
    root.addProperty(KEY_MAX_TERMINAL_RADIUS, config.maxTerminalRadius());
    root.addProperty(KEY_DEFAULT_TERMINAL_ACCESS_MODE, config.defaultTerminalAccessMode().id());
    root.addProperty(KEY_SORT_ORDERING, config.sortOrdering().id());
    root.addProperty(KEY_SORT_BUTTON_ENABLED, config.sortButtonEnabled());
    root.addProperty(KEY_QUICK_STACK_RADIUS, config.quickStackRadius());
    root.addProperty(KEY_QUICK_STACK_ENABLED, config.quickStackEnabled());
    root.addProperty(KEY_DEPOSIT_ALL_SKIPS_HOTBAR, config.depositAllSkipsHotbar());
    root.addProperty(KEY_NEAR_CAPACITY_WARNING_PERCENT, config.nearCapacityWarningPercent());
    root.addProperty(
        KEY_EXPENSIVE_OPERATION_COOLDOWN_TICKS, config.expensiveOperationCooldownTicks());
    root.addProperty(KEY_MOVES_PER_TICK_BUDGET, config.movesPerTickBudget());
    root.addProperty(KEY_LARGE_OPERATION_CONTAINERS, config.largeOperationContainers());
    final JsonObject overrides = new JsonObject();
    config.containerEligibilityOverrides().forEach(overrides::addProperty);
    root.add(KEY_CONTAINER_ELIGIBILITY_OVERRIDES, overrides);
    return root;
  }

  public static TotalStorageConfig fromJson(JsonObject root) {
    final TotalStorageConfig defaults = TotalStorageConfig.defaults();
    if (root == null) {
      return defaults;
    }
    return new TotalStorageConfig(
        boundedInt(
            root,
            KEY_MAX_TERMINAL_RADIUS,
            defaults.maxTerminalRadius(),
            TotalStorageConfig.MAX_TERMINAL_RADIUS_MIN,
            TotalStorageConfig.MAX_TERMINAL_RADIUS_MAX),
        TerminalAccessMode.fromId(JsonValues.string(root, KEY_DEFAULT_TERMINAL_ACCESS_MODE))
            .orElseGet(
                () ->
                    warnUnknown(
                        KEY_DEFAULT_TERMINAL_ACCESS_MODE,
                        root,
                        defaults.defaultTerminalAccessMode())),
        SortOrdering.fromId(JsonValues.string(root, KEY_SORT_ORDERING))
            .orElseGet(() -> warnUnknown(KEY_SORT_ORDERING, root, defaults.sortOrdering())),
        JsonValues.bool(root, KEY_SORT_BUTTON_ENABLED, defaults.sortButtonEnabled()),
        boundedInt(
            root,
            KEY_QUICK_STACK_RADIUS,
            defaults.quickStackRadius(),
            TotalStorageConfig.QUICK_STACK_RADIUS_MIN,
            TotalStorageConfig.QUICK_STACK_RADIUS_MAX),
        JsonValues.bool(root, KEY_QUICK_STACK_ENABLED, defaults.quickStackEnabled()),
        JsonValues.bool(root, KEY_DEPOSIT_ALL_SKIPS_HOTBAR, defaults.depositAllSkipsHotbar()),
        boundedInt(
            root,
            KEY_NEAR_CAPACITY_WARNING_PERCENT,
            defaults.nearCapacityWarningPercent(),
            TotalStorageConfig.NEAR_CAPACITY_WARNING_PERCENT_MIN,
            TotalStorageConfig.NEAR_CAPACITY_WARNING_PERCENT_MAX),
        boundedInt(
            root,
            KEY_EXPENSIVE_OPERATION_COOLDOWN_TICKS,
            defaults.expensiveOperationCooldownTicks(),
            TotalStorageConfig.EXPENSIVE_OPERATION_COOLDOWN_TICKS_MIN,
            TotalStorageConfig.EXPENSIVE_OPERATION_COOLDOWN_TICKS_MAX),
        boundedInt(
            root,
            KEY_MOVES_PER_TICK_BUDGET,
            defaults.movesPerTickBudget(),
            TotalStorageConfig.MOVES_PER_TICK_BUDGET_MIN,
            TotalStorageConfig.MOVES_PER_TICK_BUDGET_MAX),
        boundedInt(
            root,
            KEY_LARGE_OPERATION_CONTAINERS,
            defaults.largeOperationContainers(),
            TotalStorageConfig.LARGE_OPERATION_CONTAINERS_MIN,
            TotalStorageConfig.LARGE_OPERATION_CONTAINERS_MAX),
        overrides(root));
  }

  private static <T> T warnUnknown(String key, JsonObject root, T fallback) {
    if (root.has(key)) {
      LOGGER.warn("Unrecognised Total Storage config value for {}, using {}", key, fallback);
    }
    return fallback;
  }

  private static int boundedInt(JsonObject root, String key, int fallback, int min, int max) {
    final int value;
    try {
      value =
          root.has(key) && root.get(key).isJsonPrimitive() ? root.get(key).getAsInt() : fallback;
    } catch (RuntimeException ex) {
      LOGGER.warn("Unreadable Total Storage config value for {}, using {}", key, fallback);
      return fallback;
    }
    if (value < min || value > max) {
      LOGGER.warn(
          "Total Storage config value {}={} out of range [{}, {}], clamping", key, value, min, max);
    }
    return value;
  }

  private static Map<String, Boolean> overrides(JsonObject root) {
    if (!root.has(KEY_CONTAINER_ELIGIBILITY_OVERRIDES)
        || !root.get(KEY_CONTAINER_ELIGIBILITY_OVERRIDES).isJsonObject()) {
      return Map.of();
    }
    final Map<String, Boolean> parsed = new LinkedHashMap<>();
    for (final Map.Entry<String, JsonElement> entry :
        root.getAsJsonObject(KEY_CONTAINER_ELIGIBILITY_OVERRIDES).entrySet()) {
      if (!entry.getValue().isJsonPrimitive()) {
        LOGGER.warn("Dropping non-boolean container eligibility override for {}", entry.getKey());
        continue;
      }
      try {
        parsed.put(entry.getKey(), entry.getValue().getAsBoolean());
      } catch (RuntimeException ex) {
        LOGGER.warn("Dropping unreadable container eligibility override for {}", entry.getKey());
      }
    }
    return parsed;
  }
}
