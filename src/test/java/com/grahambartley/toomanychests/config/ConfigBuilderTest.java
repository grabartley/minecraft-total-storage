package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigBuilderTest {
  @Test
  void buildingWithoutEditingReturnsAnEqualConfig() {
    TooManyChestsConfig original = TestConfigs.everythingNonDefault();

    assertEquals(original, ConfigBuilder.from(original).build());
  }

  @Test
  void editingOneSettingLeavesEveryOtherAlone() {
    TooManyChestsConfig original = TestConfigs.everythingNonDefault();

    TooManyChestsConfig edited = ConfigBuilder.from(original).quickStackRadius(5).build();

    assertEquals(5, edited.quickStackRadius());
    assertEquals(original.maxTerminalRadius(), edited.maxTerminalRadius());
    assertEquals(original.defaultTerminalAccessMode(), edited.defaultTerminalAccessMode());
    assertEquals(original.sortOrdering(), edited.sortOrdering());
    assertEquals(original.sortButtonEnabled(), edited.sortButtonEnabled());
    assertEquals(original.middleClickSortEnabled(), edited.middleClickSortEnabled());
    assertEquals(original.depositAllSkipsHotbar(), edited.depositAllSkipsHotbar());
    assertEquals(original.nearCapacityWarningPercent(), edited.nearCapacityWarningPercent());
    assertEquals(
        original.expensiveOperationCooldownTicks(), edited.expensiveOperationCooldownTicks());
    assertEquals(original.movesPerTickBudget(), edited.movesPerTickBudget());
    assertEquals(original.containerEligibilityOverrides(), edited.containerEligibilityOverrides());
  }

  @Test
  void everySetterReachesTheBuiltConfig() {
    TooManyChestsConfig built =
        ConfigBuilder.from(TooManyChestsConfig.defaults())
            .maxTerminalRadius(24)
            .defaultTerminalAccessMode(TerminalAccessMode.TRUSTED)
            .sortOrdering(SortOrdering.DISPLAY_NAME)
            .sortButtonEnabled(false)
            .middleClickSortEnabled(false)
            .quickStackRadius(3)
            .depositAllSkipsHotbar(true)
            .nearCapacityWarningPercent(75)
            .expensiveOperationCooldownTicks(40)
            .movesPerTickBudget(128)
            .containerEligibilityOverrides(Map.of("minecraft:barrel", false))
            .build();

    assertEquals(24, built.maxTerminalRadius());
    assertEquals(TerminalAccessMode.TRUSTED, built.defaultTerminalAccessMode());
    assertEquals(SortOrdering.DISPLAY_NAME, built.sortOrdering());
    assertEquals(false, built.sortButtonEnabled());
    assertEquals(false, built.middleClickSortEnabled());
    assertEquals(3, built.quickStackRadius());
    assertEquals(true, built.depositAllSkipsHotbar());
    assertEquals(75, built.nearCapacityWarningPercent());
    assertEquals(40, built.expensiveOperationCooldownTicks());
    assertEquals(128, built.movesPerTickBudget());
    assertEquals(Map.of("minecraft:barrel", false), built.containerEligibilityOverrides());
  }

  @Test
  void editingTheSameSettingTwiceKeepsTheLastValue() {
    TooManyChestsConfig built =
        ConfigBuilder.from(TooManyChestsConfig.defaults())
            .movesPerTickBudget(8)
            .movesPerTickBudget(9)
            .build();

    assertEquals(9, built.movesPerTickBudget());
  }

  @Test
  void anOutOfRangeEditIsStillClampedByTheRecord() {
    TooManyChestsConfig built =
        ConfigBuilder.from(TooManyChestsConfig.defaults()).maxTerminalRadius(9999).build();

    assertEquals(TooManyChestsConfig.MAX_TERMINAL_RADIUS_MAX, built.maxTerminalRadius());
  }

  @Test
  void thereIsNothingToEditWithoutAConfig() {
    assertThrows(NullPointerException.class, () -> ConfigBuilder.from(null));
  }
}
