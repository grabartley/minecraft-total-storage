package com.grahambartley.toomanychests.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Every server-wide setting, per world. Out-of-range values are clamped rather than rejected, so a
 * bad hand edit never stops a server starting.
 */
public record TooManyChestsConfig(
    int maxTerminalRadius,
    TerminalAccessMode defaultTerminalAccessMode,
    SortOrdering sortOrdering,
    boolean sortButtonEnabled,
    boolean middleClickSortEnabled,
    int quickStackRadius,
    boolean depositAllSkipsHotbar,
    int nearCapacityWarningPercent,
    int expensiveOperationCooldownTicks,
    int movesPerTickBudget,
    Map<String, Boolean> containerEligibilityOverrides) {

  private static final Logger LOGGER = LoggerFactory.getLogger(TooManyChestsConfig.class);

  public static final int MAX_TERMINAL_RADIUS_MIN = 1;
  public static final int MAX_TERMINAL_RADIUS_MAX = 64;
  public static final int QUICK_STACK_RADIUS_MIN = 1;
  public static final int QUICK_STACK_RADIUS_MAX = 64;
  public static final int NEAR_CAPACITY_WARNING_PERCENT_MIN = 0;
  public static final int NEAR_CAPACITY_WARNING_PERCENT_MAX = 100;
  public static final int EXPENSIVE_OPERATION_COOLDOWN_TICKS_MIN = 0;
  public static final int EXPENSIVE_OPERATION_COOLDOWN_TICKS_MAX = 200;
  public static final int MOVES_PER_TICK_BUDGET_MIN = 1;
  public static final int MOVES_PER_TICK_BUDGET_MAX = 1024;

  public static final int DEFAULT_MAX_TERMINAL_RADIUS = 16;
  public static final TerminalAccessMode DEFAULT_TERMINAL_ACCESS_MODE = TerminalAccessMode.OWNER;
  public static final SortOrdering DEFAULT_SORT_ORDERING = SortOrdering.REGISTRY_ID;
  public static final boolean DEFAULT_SORT_BUTTON_ENABLED = true;
  public static final boolean DEFAULT_MIDDLE_CLICK_SORT_ENABLED = true;
  public static final int DEFAULT_QUICK_STACK_RADIUS = 8;
  public static final boolean DEFAULT_DEPOSIT_ALL_SKIPS_HOTBAR = false;
  public static final int DEFAULT_NEAR_CAPACITY_WARNING_PERCENT = 90;
  public static final int DEFAULT_EXPENSIVE_OPERATION_COOLDOWN_TICKS = 10;
  public static final int DEFAULT_MOVES_PER_TICK_BUDGET = 64;

  public TooManyChestsConfig {
    maxTerminalRadius = clamp(maxTerminalRadius, MAX_TERMINAL_RADIUS_MIN, MAX_TERMINAL_RADIUS_MAX);
    quickStackRadius = clamp(quickStackRadius, QUICK_STACK_RADIUS_MIN, QUICK_STACK_RADIUS_MAX);
    nearCapacityWarningPercent =
        clamp(
            nearCapacityWarningPercent,
            NEAR_CAPACITY_WARNING_PERCENT_MIN,
            NEAR_CAPACITY_WARNING_PERCENT_MAX);
    expensiveOperationCooldownTicks =
        clamp(
            expensiveOperationCooldownTicks,
            EXPENSIVE_OPERATION_COOLDOWN_TICKS_MIN,
            EXPENSIVE_OPERATION_COOLDOWN_TICKS_MAX);
    movesPerTickBudget =
        clamp(movesPerTickBudget, MOVES_PER_TICK_BUDGET_MIN, MOVES_PER_TICK_BUDGET_MAX);
    defaultTerminalAccessMode =
        defaultTerminalAccessMode == null
            ? DEFAULT_TERMINAL_ACCESS_MODE
            : defaultTerminalAccessMode;
    sortOrdering = sortOrdering == null ? DEFAULT_SORT_ORDERING : sortOrdering;
    containerEligibilityOverrides = normalizedOverrides(containerEligibilityOverrides);
  }

  public static TooManyChestsConfig defaults() {
    return new TooManyChestsConfig(
        DEFAULT_MAX_TERMINAL_RADIUS,
        DEFAULT_TERMINAL_ACCESS_MODE,
        DEFAULT_SORT_ORDERING,
        DEFAULT_SORT_BUTTON_ENABLED,
        DEFAULT_MIDDLE_CLICK_SORT_ENABLED,
        DEFAULT_QUICK_STACK_RADIUS,
        DEFAULT_DEPOSIT_ALL_SKIPS_HOTBAR,
        DEFAULT_NEAR_CAPACITY_WARNING_PERCENT,
        DEFAULT_EXPENSIVE_OPERATION_COOLDOWN_TICKS,
        DEFAULT_MOVES_PER_TICK_BUDGET,
        Map.of());
  }

  /** Whether an operator has forced this block's eligibility either way, if they have. */
  public Optional<Boolean> containerEligibilityOverrideFor(Identifier blockId) {
    return blockId == null
        ? Optional.empty()
        : Optional.ofNullable(containerEligibilityOverrides.get(blockId.toString()));
  }

  private static Map<String, Boolean> normalizedOverrides(@Nullable Map<String, Boolean> raw) {
    if (raw == null || raw.isEmpty()) {
      return Map.of();
    }
    final Map<String, Boolean> normalized = new LinkedHashMap<>();
    for (final Map.Entry<String, Boolean> entry : raw.entrySet()) {
      final Identifier blockId =
          entry.getKey() == null ? null : Identifier.tryParse(entry.getKey());
      if (blockId == null || entry.getValue() == null) {
        LOGGER.warn("Dropping unusable container eligibility override for {}", entry.getKey());
        continue;
      }
      normalized.put(blockId.toString(), entry.getValue());
    }
    return Collections.unmodifiableMap(normalized);
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }
}
