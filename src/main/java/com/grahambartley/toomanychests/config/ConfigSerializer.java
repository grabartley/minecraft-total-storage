package com.grahambartley.toomanychests.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Translates between the config record and its on-disk JSON shape. */
public final class ConfigSerializer {
  static final String KEY_MAX_TERMINAL_RADIUS = "maxTerminalRadius";
  static final String KEY_DEFAULT_TERMINAL_ACCESS_MODE = "defaultTerminalAccessMode";
  static final String KEY_SORT_ORDERING = "sortOrdering";
  static final String KEY_SORT_BUTTON_ENABLED = "sortButtonEnabled";
  static final String KEY_MIDDLE_CLICK_SORT_ENABLED = "middleClickSortEnabled";
  static final String KEY_QUICK_STACK_RADIUS = "quickStackRadius";
  static final String KEY_DEPOSIT_ALL_SKIPS_HOTBAR = "depositAllSkipsHotbar";
  static final String KEY_NEAR_CAPACITY_WARNING_PERCENT = "nearCapacityWarningPercent";
  static final String KEY_EXPENSIVE_OPERATION_COOLDOWN_TICKS = "expensiveOperationCooldownTicks";
  static final String KEY_MOVES_PER_TICK_BUDGET = "movesPerTickBudget";
  static final String KEY_CONTAINER_ELIGIBILITY_OVERRIDES = "containerEligibilityOverrides";

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigSerializer.class);

  private ConfigSerializer() {}

  public static JsonObject toJson(TooManyChestsConfig config) {
    final JsonObject root = new JsonObject();
    root.addProperty(KEY_MAX_TERMINAL_RADIUS, config.maxTerminalRadius());
    root.addProperty(KEY_DEFAULT_TERMINAL_ACCESS_MODE, config.defaultTerminalAccessMode().id());
    root.addProperty(KEY_SORT_ORDERING, config.sortOrdering().id());
    root.addProperty(KEY_SORT_BUTTON_ENABLED, config.sortButtonEnabled());
    root.addProperty(KEY_MIDDLE_CLICK_SORT_ENABLED, config.middleClickSortEnabled());
    root.addProperty(KEY_QUICK_STACK_RADIUS, config.quickStackRadius());
    root.addProperty(KEY_DEPOSIT_ALL_SKIPS_HOTBAR, config.depositAllSkipsHotbar());
    root.addProperty(KEY_NEAR_CAPACITY_WARNING_PERCENT, config.nearCapacityWarningPercent());
    root.addProperty(
        KEY_EXPENSIVE_OPERATION_COOLDOWN_TICKS, config.expensiveOperationCooldownTicks());
    root.addProperty(KEY_MOVES_PER_TICK_BUDGET, config.movesPerTickBudget());
    final JsonObject overrides = new JsonObject();
    config.containerEligibilityOverrides().forEach(overrides::addProperty);
    root.add(KEY_CONTAINER_ELIGIBILITY_OVERRIDES, overrides);
    return root;
  }

  public static TooManyChestsConfig fromJson(JsonObject root) {
    final TooManyChestsConfig defaults = TooManyChestsConfig.defaults();
    if (root == null) {
      return defaults;
    }
    return new TooManyChestsConfig(
        boundedInt(
            root,
            KEY_MAX_TERMINAL_RADIUS,
            defaults.maxTerminalRadius(),
            TooManyChestsConfig.MAX_TERMINAL_RADIUS_MIN,
            TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX),
        TerminalAccessMode.fromId(string(root, KEY_DEFAULT_TERMINAL_ACCESS_MODE))
            .orElseGet(
                () ->
                    warnUnknown(
                        KEY_DEFAULT_TERMINAL_ACCESS_MODE,
                        root,
                        defaults.defaultTerminalAccessMode())),
        SortOrdering.fromId(string(root, KEY_SORT_ORDERING))
            .orElseGet(() -> warnUnknown(KEY_SORT_ORDERING, root, defaults.sortOrdering())),
        bool(root, KEY_SORT_BUTTON_ENABLED, defaults.sortButtonEnabled()),
        bool(root, KEY_MIDDLE_CLICK_SORT_ENABLED, defaults.middleClickSortEnabled()),
        boundedInt(
            root,
            KEY_QUICK_STACK_RADIUS,
            defaults.quickStackRadius(),
            TooManyChestsConfig.QUICK_STACK_RADIUS_MIN,
            TooManyChestsConfig.QUICK_STACK_RADIUS_MAX),
        bool(root, KEY_DEPOSIT_ALL_SKIPS_HOTBAR, defaults.depositAllSkipsHotbar()),
        boundedInt(
            root,
            KEY_NEAR_CAPACITY_WARNING_PERCENT,
            defaults.nearCapacityWarningPercent(),
            TooManyChestsConfig.NEAR_CAPACITY_WARNING_PERCENT_MIN,
            TooManyChestsConfig.NEAR_CAPACITY_WARNING_PERCENT_MAX),
        boundedInt(
            root,
            KEY_EXPENSIVE_OPERATION_COOLDOWN_TICKS,
            defaults.expensiveOperationCooldownTicks(),
            TooManyChestsConfig.EXPENSIVE_OPERATION_COOLDOWN_TICKS_MIN,
            TooManyChestsConfig.EXPENSIVE_OPERATION_COOLDOWN_TICKS_MAX),
        boundedInt(
            root,
            KEY_MOVES_PER_TICK_BUDGET,
            defaults.movesPerTickBudget(),
            TooManyChestsConfig.MOVES_PER_TICK_BUDGET_MIN,
            TooManyChestsConfig.MOVES_PER_TICK_BUDGET_MAX),
        overrides(root));
  }

  private static <T> T warnUnknown(String key, JsonObject root, T fallback) {
    if (root.has(key)) {
      LOGGER.warn("Unrecognised Too Many Chests config value for {}, using {}", key, fallback);
    }
    return fallback;
  }

  private static String string(JsonObject root, String key) {
    return root.has(key) && root.get(key).isJsonPrimitive() ? root.get(key).getAsString() : null;
  }

  private static boolean bool(JsonObject root, String key, boolean fallback) {
    try {
      return root.has(key) && root.get(key).isJsonPrimitive()
          ? root.get(key).getAsBoolean()
          : fallback;
    } catch (RuntimeException ex) {
      LOGGER.warn("Unreadable Too Many Chests config value for {}, using {}", key, fallback);
      return fallback;
    }
  }

  private static int boundedInt(JsonObject root, String key, int fallback, int min, int max) {
    final int value;
    try {
      value =
          root.has(key) && root.get(key).isJsonPrimitive() ? root.get(key).getAsInt() : fallback;
    } catch (RuntimeException ex) {
      LOGGER.warn("Unreadable Too Many Chests config value for {}, using {}", key, fallback);
      return fallback;
    }
    if (value < min || value > max) {
      LOGGER.warn(
          "Too Many Chests config value {}={} out of range [{}, {}], clamping",
          key,
          value,
          min,
          max);
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
