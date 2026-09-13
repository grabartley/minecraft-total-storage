package com.grahambartley.toomanychests.config;

import java.util.List;
import java.util.Locale;

public enum ConfigSection {
  DEFAULTS(List.of("maxTerminalRadius", "defaultTerminalAccessMode", "quickStackRadius")),
  SORTING(List.of("sortOrdering", "sortButtonEnabled", "middleClickSortEnabled")),
  FEATURES(List.of("quickStackEnabled", "depositAllSkipsHotbar", "nearCapacityWarningPercent")),
  PERFORMANCE(
      List.of("expensiveOperationCooldownTicks", "movesPerTickBudget", "largeOperationContainers"));

  private final List<String> settingNames;

  ConfigSection(List<String> settingNames) {
    this.settingNames = List.copyOf(settingNames);
  }

  public String id() {
    return name().toLowerCase(Locale.ROOT);
  }

  public List<ConfigSetting> settings() {
    return settingNames.stream().map(name -> ConfigSettings.byName(name).orElseThrow()).toList();
  }
}
