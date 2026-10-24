package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
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
          TotalStorageConfig::defaultTerminalAccessMode,
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
            ConfigBuilder.from(TotalStorageConfig.defaults())
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
    Optional<TotalStorageConfig> updated = setting.withValue(TotalStorageConfig.defaults(), given);

    assertTrue(updated.isPresent(), given + " should be accepted");
    assertEquals(expected, updated.get().defaultTerminalAccessMode());
  }

  @ParameterizedTest
  @ValueSource(strings = {"everyone", "", "  ", "OWNER_ONLY", "0"})
  void itRefusesAnythingThatNamesNoAlternative(String given) {
    assertEquals(Optional.empty(), setting.withValue(TotalStorageConfig.defaults(), given));
  }

  @ParameterizedTest
  @NullSource
  void itRefusesAnAbsentValue(String given) {
    assertEquals(Optional.empty(), setting.withValue(TotalStorageConfig.defaults(), given));
  }

  @Test
  void itWorksForEverySettingEnumNotJustOne() {
    ConfigSetting ordering =
        new EnumConfigSetting<>(
            "sortOrdering",
            SortOrdering.class,
            TotalStorageConfig::sortOrdering,
            ConfigBuilder::sortOrdering,
            SortOrdering.REGISTRY_ID);

    assertEquals(List.of("registry_id", "display_name"), ordering.suggestions());
    assertEquals(
        SortOrdering.DISPLAY_NAME,
        ordering
            .withValue(TotalStorageConfig.defaults(), "display_name")
            .orElseThrow()
            .sortOrdering());
  }

  @Test
  void resettingRestoresTheDefault() {
    TotalStorageConfig away =
        ConfigBuilder.from(TotalStorageConfig.defaults())
            .defaultTerminalAccessMode(TerminalAccessMode.OPEN)
            .build();

    assertEquals(TerminalAccessMode.OWNER, setting.reset(away).defaultTerminalAccessMode());
  }

  @Test
  void itLeavesEveryOtherSettingAlone() {
    TotalStorageConfig original = TestConfigs.everythingNonDefault();

    TotalStorageConfig updated = setting.withValue(original, "trusted").orElseThrow();

    assertEquals(
        ConfigBuilder.from(original).defaultTerminalAccessMode(TerminalAccessMode.TRUSTED).build(),
        updated);
  }

  @ParameterizedTest
  @CsvSource({"owner, OWNER", "TRUSTED, TRUSTED", "' open ', OPEN"})
  void aValueIsShownByTheModesOwnName(String raw, TerminalAccessMode mode) {
    assertEquals(mode.displayName(), setting.displayValue(raw));
  }

  @Test
  void aValueNoModeAnswersToIsShownAsItWasWritten() {
    assertEquals(Text.literal("everyone"), setting.displayValue("everyone"));
  }
}
