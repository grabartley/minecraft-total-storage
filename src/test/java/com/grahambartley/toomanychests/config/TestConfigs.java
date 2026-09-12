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
    return ConfigBuilder.from(TooManyChestsConfig.defaults())
        .maxTerminalRadius(maxTerminalRadius)
        .quickStackRadius(quickStackRadius)
        .nearCapacityWarningPercent(nearCapacityWarningPercent)
        .expensiveOperationCooldownTicks(expensiveOperationCooldownTicks)
        .movesPerTickBudget(movesPerTickBudget)
        .build();
  }

  public static TooManyChestsConfig withOverrides(Map<String, Boolean> overrides) {
    return ConfigBuilder.from(TooManyChestsConfig.defaults())
        .containerEligibilityOverrides(overrides)
        .build();
  }

  public static TooManyChestsConfig withModes(
      TerminalAccessMode accessMode, SortOrdering ordering) {
    return ConfigBuilder.from(TooManyChestsConfig.defaults())
        .defaultTerminalAccessMode(accessMode)
        .sortOrdering(ordering)
        .build();
  }

  public static TooManyChestsConfig everythingNonDefault() {
    return ConfigBuilder.from(TooManyChestsConfig.defaults())
        .maxTerminalRadius(32)
        .defaultTerminalAccessMode(TerminalAccessMode.OPEN)
        .sortOrdering(SortOrdering.DISPLAY_NAME)
        .sortButtonEnabled(false)
        .middleClickSortEnabled(false)
        .quickStackRadius(4)
        .depositAllSkipsHotbar(true)
        .nearCapacityWarningPercent(50)
        .expensiveOperationCooldownTicks(40)
        .movesPerTickBudget(128)
        .largeOperationContainers(24)
        .containerEligibilityOverrides(Map.of("minecraft:furnace", true, "minecraft:barrel", false))
        .build();
  }
}
