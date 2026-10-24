package com.grahambartley.totalstorage.config;

import java.util.Map;

public final class TestConfigs {
  private TestConfigs() {}

  public static TotalStorageConfig withInts(
      int maxTerminalRadius,
      int quickStackRadius,
      int nearCapacityWarningPercent,
      int expensiveOperationCooldownTicks,
      int movesPerTickBudget) {
    return ConfigBuilder.from(TotalStorageConfig.defaults())
        .maxTerminalRadius(maxTerminalRadius)
        .quickStackRadius(quickStackRadius)
        .nearCapacityWarningPercent(nearCapacityWarningPercent)
        .expensiveOperationCooldownTicks(expensiveOperationCooldownTicks)
        .movesPerTickBudget(movesPerTickBudget)
        .build();
  }

  public static TotalStorageConfig withOverrides(Map<String, Boolean> overrides) {
    return ConfigBuilder.from(TotalStorageConfig.defaults())
        .containerEligibilityOverrides(overrides)
        .build();
  }

  public static TotalStorageConfig withModes(TerminalAccessMode accessMode, SortOrdering ordering) {
    return ConfigBuilder.from(TotalStorageConfig.defaults())
        .defaultTerminalAccessMode(accessMode)
        .sortOrdering(ordering)
        .build();
  }

  public static TotalStorageConfig everythingNonDefault() {
    return ConfigBuilder.from(TotalStorageConfig.defaults())
        .maxTerminalRadius(32)
        .defaultTerminalAccessMode(TerminalAccessMode.OPEN)
        .sortOrdering(SortOrdering.DISPLAY_NAME)
        .sortButtonEnabled(false)
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
