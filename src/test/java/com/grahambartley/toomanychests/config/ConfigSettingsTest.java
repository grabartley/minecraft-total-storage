package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ConfigSettingsTest {
  static List<ConfigSetting> everySetting() {
    return ConfigSettings.all();
  }

  @Test
  void theNamesReadInTheSameOrderAsTheSettings() {
    assertEquals(
        ConfigSettings.all().stream().map(ConfigSetting::name).toList(), ConfigSettings.names());
  }

  @Test
  void noTwoSettingsShareAName() {
    assertEquals(ConfigSettings.names().size(), ConfigSettings.names().stream().distinct().count());
  }

  @ParameterizedTest
  @MethodSource("everySetting")
  void everySettingIsFoundByItsOwnName(ConfigSetting setting) {
    assertEquals(Optional.of(setting), ConfigSettings.byName(setting.name()));
  }

  @ParameterizedTest
  @MethodSource("everySetting")
  void everySettingIsFoundHoweverItIsCased(ConfigSetting setting) {
    assertEquals(
        Optional.of(setting), ConfigSettings.byName("  " + setting.name().toUpperCase() + "  "));
  }

  @ParameterizedTest
  @MethodSource("everySetting")
  void everySettingReadsItsOwnDefaultOutOfTheDefaultConfig(ConfigSetting setting) {
    assertEquals(setting.defaultValue(), setting.valueIn(TooManyChestsConfig.defaults()));
  }

  @ParameterizedTest
  @MethodSource("everySetting")
  void everySettingTakesEverySuggestionItOffers(ConfigSetting setting) {
    for (final String suggestion : setting.suggestions()) {
      Optional<TooManyChestsConfig> updated =
          setting.withValue(TooManyChestsConfig.defaults(), suggestion);

      assertTrue(
          updated.isPresent(), setting.name() + " should accept its own suggestion " + suggestion);
      assertEquals(suggestion, setting.valueIn(updated.get()));
    }
  }

  @ParameterizedTest
  @MethodSource("everySetting")
  void everySettingCanBeMovedAwayFromItsDefaultAndBack(ConfigSetting setting) {
    String away =
        setting.suggestions().stream()
            .filter(suggestion -> !suggestion.equals(setting.defaultValue()))
            .findFirst()
            .orElseThrow();

    TooManyChestsConfig moved =
        setting.withValue(TooManyChestsConfig.defaults(), away).orElseThrow();

    assertNotEquals(TooManyChestsConfig.defaults(), moved);
    assertEquals(TooManyChestsConfig.defaults(), setting.reset(moved));
  }

  /**
   * The guard against a setting existing in the config record and nowhere else. Resetting the list
   * one by one only lands back on the defaults if the list reaches every field the fixture moved.
   */
  @Test
  void resettingEverySettingReturnsAConfigToItsDefaults() {
    TooManyChestsConfig away = TestConfigs.everythingNonDefault();

    TooManyChestsConfig reset = away;
    for (final ConfigSetting setting : ConfigSettings.all()) {
      reset = setting.reset(reset);
    }

    assertEquals(
        TooManyChestsConfig.defaults()
            .withContainerEligibilityOverrides(away.containerEligibilityOverrides()),
        reset);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "  ", "notASetting", "max_terminal_radius"})
  void aNameNoSettingAnswersToIsNotFound(String name) {
    assertEquals(Optional.empty(), ConfigSettings.byName(name));
  }

  @Test
  void anAbsentNameIsNotFound() {
    assertEquals(Optional.empty(), ConfigSettings.byName(null));
  }
}
