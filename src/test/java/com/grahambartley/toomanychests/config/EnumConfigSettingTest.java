package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EnumConfigSettingTest {
  private final ConfigSetting setting =
      new EnumConfigSetting<>(
          "defaultTerminalAccessMode",
          TerminalAccessMode.class,
          TooManyChestsConfig::defaultTerminalAccessMode,
          ConfigBuilder::defaultTerminalAccessMode,
          TerminalAccessMode.OWNER);

  @Test
  void itIsNamedAfterTheSettingItMoves() {
    assertEquals("defaultTerminalAccessMode", setting.name());
  }

  @Test
  void itReadsTheValueOutOfAConfigByItsId() {
    assertEquals(
        "open",
        setting.valueIn(
            ConfigBuilder.from(TooManyChestsConfig.defaults())
                .defaultTerminalAccessMode(TerminalAccessMode.OPEN)
                .build()));
  }

  @Test
  void itReportsItsDocumentedDefault() {
    assertEquals("owner", setting.defaultValue());
  }

  @Test
  void itOffersEveryAlternativeInDeclarationOrder() {
    assertEquals(List.of("owner", "trusted", "open"), setting.suggestions());
  }

  @Test
  void itNamesEveryAlternativeItWillAccept() {
    assertEquals("owner, trusted, open", setting.allowedValues());
  }

  @ParameterizedTest
  @CsvSource({
    "owner, OWNER",
    "trusted, TRUSTED",
    "open, OPEN",
    "OPEN, OPEN",
    "' Trusted ', TRUSTED"
  })
  void itTakesAnyAlternativeIdInAnyCase(String given, TerminalAccessMode expected) {
    Optional<TooManyChestsConfig> updated =
        setting.withValue(TooManyChestsConfig.defaults(), given);

    assertTrue(updated.isPresent(), given + " should be accepted");
    assertEquals(expected, updated.get().defaultTerminalAccessMode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"everyone", "", "  ", "OWNER_ONLY", "0"})
  void itRefusesAnythingThatNamesNoAlternative(String given) {
    assertEquals(Optional.empty(), setting.withValue(TooManyChestsConfig.defaults(), given));
  }

  @ParameterizedTest
  @NullSource
  void itRefusesAnAbsentValue(String given) {
    assertEquals(Optional.empty(), setting.withValue(TooManyChestsConfig.defaults(), given));
  }

  @Test
  void itWorksForEverySettingEnumNotJustOne() {
    ConfigSetting ordering =
        new EnumConfigSetting<>(
            "sortOrdering",
            SortOrdering.class,
            TooManyChestsConfig::sortOrdering,
            ConfigBuilder::sortOrdering,
            SortOrdering.REGISTRY_ID);

    assertEquals(List.of("registry_id", "display_name"), ordering.suggestions());
    assertEquals(
        SortOrdering.DISPLAY_NAME,
        ordering
            .withValue(TooManyChestsConfig.defaults(), "display_name")
            .orElseThrow()
            .sortOrdering());
  }

  @Test
  void resettingRestoresTheDefault() {
    TooManyChestsConfig away =
        ConfigBuilder.from(TooManyChestsConfig.defaults())
            .defaultTerminalAccessMode(TerminalAccessMode.OPEN)
            .build();

    assertEquals(TerminalAccessMode.OWNER, setting.reset(away).defaultTerminalAccessMode());
  }

  @Test
  void itLeavesEveryOtherSettingAlone() {
    TooManyChestsConfig original = TestConfigs.everythingNonDefault();

    TooManyChestsConfig updated = setting.withValue(original, "trusted").orElseThrow();

    assertEquals(
        ConfigBuilder.from(original).defaultTerminalAccessMode(TerminalAccessMode.TRUSTED).build(),
        updated);
  }
}
