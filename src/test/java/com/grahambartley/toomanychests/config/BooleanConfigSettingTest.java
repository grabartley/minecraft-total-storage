package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import net.minecraft.screen.ScreenTexts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BooleanConfigSettingTest {
  private final ConfigSetting setting =
      new BooleanConfigSetting(
          "sortButtonEnabled",
          TooManyChestsConfig::sortButtonEnabled,
          ConfigBuilder::sortButtonEnabled,
          true);

  @Test
  void itIsNamedAfterTheSettingItMoves() {
    assertEquals("sortButtonEnabled", setting.name());
  }

  @Test
  void itReadsTheValueOutOfAConfig() {
    assertEquals(
        "false", setting.valueIn(ConfigBuilder.from(defaults()).sortButtonEnabled(false).build()));
  }

  @Test
  void itReportsItsDocumentedDefault() {
    assertEquals("true", setting.defaultValue());
  }

  @Test
  void itOffersBothAlternatives() {
    assertEquals(List.of("true", "false"), setting.suggestions());
  }

  @ParameterizedTest
  @CsvSource({"true, true", "false, false", "TRUE, true", "False, false", "' true ', true"})
  void itTakesEitherSpellingInAnyCase(String given, boolean expected) {
    Optional<TooManyChestsConfig> updated = setting.withValue(defaults(), given);

    assertTrue(updated.isPresent(), given + " should be accepted");
    assertEquals(expected, updated.get().sortButtonEnabled());
  }

  @ParameterizedTest
  @ValueSource(strings = {"yes", "no", "1", "0", "", "  ", "truthy"})
  void itRefusesAnythingThatIsNotTrueOrFalse(String given) {
    assertEquals(Optional.empty(), setting.withValue(defaults(), given));
  }

  @ParameterizedTest
  @NullSource
  void itRefusesAnAbsentValue(String given) {
    assertEquals(Optional.empty(), setting.withValue(defaults(), given));
  }

  @Test
  void resettingRestoresTheDefault() {
    TooManyChestsConfig away = ConfigBuilder.from(defaults()).sortButtonEnabled(false).build();

    assertEquals(true, setting.reset(away).sortButtonEnabled());
  }

  @Test
  void itLeavesEveryOtherSettingAlone() {
    TooManyChestsConfig original = TestConfigs.everythingNonDefault();

    TooManyChestsConfig updated = setting.withValue(original, "true").orElseThrow();

    assertEquals(ConfigBuilder.from(original).sortButtonEnabled(true).build(), updated);
  }

  private static TooManyChestsConfig defaults() {
    return TooManyChestsConfig.defaults();
  }

  @ParameterizedTest
  @CsvSource({"true, true", "TRUE, true", "' false ', false", "false, false"})
  void aValueIsShownAsVanillasOnOrOff(String raw, boolean on) {
    assertEquals(on ? ScreenTexts.ON : ScreenTexts.OFF, setting.displayValue(raw));
  }
}
