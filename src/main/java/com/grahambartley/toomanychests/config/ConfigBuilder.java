package com.grahambartley.toomanychests.config;

import java.util.Map;
import java.util.Objects;

public final class ConfigBuilder {
  private int maxTerminalRadius;
  private TerminalAccessMode defaultTerminalAccessMode;
  private SortOrdering sortOrdering;
  private boolean sortButtonEnabled;
  private boolean middleClickSortEnabled;
  private int quickStackRadius;
  private boolean quickStackEnabled;
  private boolean depositAllSkipsHotbar;
  private int nearCapacityWarningPercent;
  private int expensiveOperationCooldownTicks;
  private int movesPerTickBudget;
  private int largeOperationContainers;
  private Map<String, Boolean> containerEligibilityOverrides;

  private ConfigBuilder(TooManyChestsConfig config) {
    maxTerminalRadius = config.maxTerminalRadius();
    defaultTerminalAccessMode = config.defaultTerminalAccessMode();
    sortOrdering = config.sortOrdering();
    sortButtonEnabled = config.sortButtonEnabled();
    middleClickSortEnabled = config.middleClickSortEnabled();
    quickStackRadius = config.quickStackRadius();
    quickStackEnabled = config.quickStackEnabled();
    depositAllSkipsHotbar = config.depositAllSkipsHotbar();
    nearCapacityWarningPercent = config.nearCapacityWarningPercent();
    expensiveOperationCooldownTicks = config.expensiveOperationCooldownTicks();
    movesPerTickBudget = config.movesPerTickBudget();
    largeOperationContainers = config.largeOperationContainers();
    containerEligibilityOverrides = config.containerEligibilityOverrides();
  }

  public static ConfigBuilder from(TooManyChestsConfig config) {
    return new ConfigBuilder(Objects.requireNonNull(config, "config"));
  }

  public ConfigBuilder maxTerminalRadius(int value) {
    maxTerminalRadius = value;
    return this;
  }

  public ConfigBuilder defaultTerminalAccessMode(TerminalAccessMode value) {
    defaultTerminalAccessMode = value;
    return this;
  }

  public ConfigBuilder sortOrdering(SortOrdering value) {
    sortOrdering = value;
    return this;
  }

  public ConfigBuilder sortButtonEnabled(boolean value) {
    sortButtonEnabled = value;
    return this;
  }

  public ConfigBuilder middleClickSortEnabled(boolean value) {
    middleClickSortEnabled = value;
    return this;
  }

  public ConfigBuilder quickStackRadius(int value) {
    quickStackRadius = value;
    return this;
  }

  public ConfigBuilder quickStackEnabled(boolean value) {
    quickStackEnabled = value;
    return this;
  }

  public ConfigBuilder depositAllSkipsHotbar(boolean value) {
    depositAllSkipsHotbar = value;
    return this;
  }

  public ConfigBuilder nearCapacityWarningPercent(int value) {
    nearCapacityWarningPercent = value;
    return this;
  }

  public ConfigBuilder expensiveOperationCooldownTicks(int value) {
    expensiveOperationCooldownTicks = value;
    return this;
  }

  public ConfigBuilder movesPerTickBudget(int value) {
    movesPerTickBudget = value;
    return this;
  }

  public ConfigBuilder largeOperationContainers(int value) {
    largeOperationContainers = value;
    return this;
  }

  public ConfigBuilder containerEligibilityOverrides(Map<String, Boolean> value) {
    containerEligibilityOverrides = value;
    return this;
  }

  public TooManyChestsConfig build() {
    return new TooManyChestsConfig(
        maxTerminalRadius,
        defaultTerminalAccessMode,
        sortOrdering,
        sortButtonEnabled,
        middleClickSortEnabled,
        quickStackRadius,
        quickStackEnabled,
        depositAllSkipsHotbar,
        nearCapacityWarningPercent,
        expensiveOperationCooldownTicks,
        movesPerTickBudget,
        largeOperationContainers,
        containerEligibilityOverrides);
  }
}
