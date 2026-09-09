package com.grahambartley.toomanychests.config;

import java.util.Map;

public final class TestConfigs {
  private TestConfigs() {}

  public static TooManyChestsConfig withInts(
      int maxTerminalRadius,
      int quickStackRadius,
      int nearCapacityWarningPercent,
      int expensiveOperationCooldownTicks,
      int movesPerTickBudget) {
    return new TooManyChestsConfig(
        maxTerminalRadius,
        TooManyChestsConfig.DEFAULT_TERMINAL_ACCESS_MODE,
        TooManyChestsConfig.DEFAULT_SORT_ORDERING,
        TooManyChestsConfig.DEFAULT_SORT_BUTTON_ENABLED,
        TooManyChestsConfig.DEFAULT_MIDDLE_CLICK_SORT_ENABLED,
        quickStackRadius,
        TooManyChestsConfig.DEFAULT_DEPOSIT_ALL_SKIPS_HOTBAR,
        nearCapacityWarningPercent,
        expensiveOperationCooldownTicks,
        movesPerTickBudget,
        Map.of());
  }

  public static TooManyChestsConfig withOverrides(Map<String, Boolean> overrides) {
    TooManyChestsConfig defaults = TooManyChestsConfig.defaults();
    return new TooManyChestsConfig(
        defaults.maxTerminalRadius(),
        defaults.defaultTerminalAccessMode(),
        defaults.sortOrdering(),
        defaults.sortButtonEnabled(),
        defaults.middleClickSortEnabled(),
        defaults.quickStackRadius(),
        defaults.depositAllSkipsHotbar(),
        defaults.nearCapacityWarningPercent(),
        defaults.expensiveOperationCooldownTicks(),
        defaults.movesPerTickBudget(),
        overrides);
  }

  public static TooManyChestsConfig withModes(
      TerminalAccessMode accessMode, SortOrdering ordering) {
    TooManyChestsConfig defaults = TooManyChestsConfig.defaults();
    return new TooManyChestsConfig(
        defaults.maxTerminalRadius(),
        accessMode,
        ordering,
        defaults.sortButtonEnabled(),
        defaults.middleClickSortEnabled(),
        defaults.quickStackRadius(),
        defaults.depositAllSkipsHotbar(),
        defaults.nearCapacityWarningPercent(),
        defaults.expensiveOperationCooldownTicks(),
        defaults.movesPerTickBudget(),
        Map.of());
  }

  public static TooManyChestsConfig everythingNonDefault() {
    return new TooManyChestsConfig(
        32,
        TerminalAccessMode.OPEN,
        SortOrdering.DISPLAY_NAME,
        false,
        false,
        4,
        true,
        50,
        40,
        128,
        Map.of("minecraft:furnace", true, "minecraft:barrel", false));
  }
}
