package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
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
    assertEquals(setting.defaultValue(), setting.valueIn(TotalStorageConfig.defaults()));
  }

  @ParameterizedTest
  @MethodSource("everySetting")
  void everySettingTakesEverySuggestionItOffers(ConfigSetting setting) {
    for (final String suggestion : setting.suggestions()) {
      Optional<TotalStorageConfig> updated =
          setting.withValue(TotalStorageConfig.defaults(), suggestion);

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

    TotalStorageConfig moved = setting.withValue(TotalStorageConfig.defaults(), away).orElseThrow();

    assertNotEquals(TotalStorageConfig.defaults(), moved);
    assertEquals(TotalStorageConfig.defaults(), setting.reset(moved));
  }

  @Test
  void resettingEverySettingReturnsAConfigToItsDefaults() {
    TotalStorageConfig away = TestConfigs.everythingNonDefault();

    TotalStorageConfig reset = away;
    for (final ConfigSetting setting : ConfigSettings.all()) {
      reset = setting.reset(reset);
    }

    assertEquals(
        TotalStorageConfig.defaults()
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

  @Test
  void theNearCapacityWarningIsShownAsAPercentage() {
    ConfigSetting warning = ConfigSettings.byName("nearCapacityWarningPercent").orElseThrow();

    assertEquals(
        "screen.total-storage.config.value.percent",
        ((TranslatableTextContent) warning.displayValue("90").getContent()).getKey());
  }

  @ParameterizedTest
  @MethodSource("everySetting")
  void noSettingShowsARawConfigIdForAnyValueItOffers(ConfigSetting setting) {
    for (String raw : setting.suggestions()) {
      if (!raw.matches("-?\\d+")) {
        assertNotEquals(
            Text.literal(raw), setting.displayValue(raw), setting.name() + " shows raw " + raw);
      }
    }
  }
}
