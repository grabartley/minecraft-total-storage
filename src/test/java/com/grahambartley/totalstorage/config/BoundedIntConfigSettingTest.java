package com.grahambartley.totalstorage.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.EnglishLang;
import java.util.List;
import java.util.Optional;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class BoundedIntConfigSettingTest {
  private final ConfigSetting setting =
      new BoundedIntConfigSetting(
          "quickStackRadius",
          1,
          64,
          8,
          TotalStorageConfig::quickStackRadius,
          ConfigBuilder::quickStackRadius);

  @Test
  void itIsNamedAfterTheSettingItMoves() {
    assertEquals("quickStackRadius", setting.name());
  }

  @Test
  void itReadsTheValueOutOfAConfig() {
    assertEquals("4", setting.valueIn(ConfigBuilder.from(defaults()).quickStackRadius(4).build()));
  }

  @Test
  void itReportsItsDocumentedDefault() {
    assertEquals("8", setting.defaultValue());
  }

  @Test
  void itOffersTheEndsOfTheRangeAndTheDefaultInOrder() {
    assertEquals(List.of("1", "8", "64"), setting.suggestions());
  }

  @Test
  void aRangeWhoseDefaultIsAnEndDoesNotOfferItTwice() {
    ConfigSetting atTheBottom =
        new BoundedIntConfigSetting(
            "movesPerTickBudget",
            1,
            16,
            1,
            TotalStorageConfig::movesPerTickBudget,
            ConfigBuilder::movesPerTickBudget);

    assertEquals(List.of("1", "16"), atTheBottom.suggestions());
  }

  @Test
  void itNamesTheRangeItWillAccept() {
    assertEquals("a whole number from 1 to 64", setting.allowedValues());
  }

  @ParameterizedTest
  @CsvSource({"1, 1", "8, 8", "64, 64", "' 12 ', 12"})
  void itTakesAnyValueInsideTheRange(String given, int expected) {
    Optional<TotalStorageConfig> updated = setting.withValue(defaults(), given);

    assertTrue(updated.isPresent(), given + " should be accepted");
    assertEquals(expected, updated.get().quickStackRadius());
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-1", "65", "1000"})
  void itRefusesAValueOutsideTheRangeRatherThanClampingIt(String given) {
    assertEquals(Optional.empty(), setting.withValue(defaults(), given));
  }

  @ParameterizedTest
  @ValueSource(strings = {"eight", "", "  ", "8.5", "8blocks", "9999999999999999999"})
  void itRefusesAnythingThatIsNotAWholeNumber(String given) {
    assertEquals(Optional.empty(), setting.withValue(defaults(), given));
  }

  @ParameterizedTest
  @NullSource
  void itRefusesAnAbsentValue(String given) {
    assertEquals(Optional.empty(), setting.withValue(defaults(), given));
  }

  @Test
  void resettingRestoresTheDefault() {
    TotalStorageConfig away = ConfigBuilder.from(defaults()).quickStackRadius(2).build();

    assertEquals(8, setting.reset(away).quickStackRadius());
  }

  @Test
  void itLeavesEveryOtherSettingAlone() {
    TotalStorageConfig original = TestConfigs.everythingNonDefault();

    TotalStorageConfig updated = setting.withValue(original, "12").orElseThrow();

    assertEquals(ConfigBuilder.from(original).quickStackRadius(12).build(), updated);
  }

  private static TotalStorageConfig defaults() {
    return TotalStorageConfig.defaults();
  }

  @Test
  void aPlainNumberIsShownAsTheNumber() {
    assertEquals(Text.literal("12"), setting.displayValue("12"));
  }

  @Test
  void aPercentageIsShownWithItsPercentSign() {
    ConfigSetting percent =
        new BoundedIntConfigSetting(
            "nearCapacityWarningPercent",
            50,
            100,
            90,
            TotalStorageConfig::nearCapacityWarningPercent,
            ConfigBuilder::nearCapacityWarningPercent,
            ConfigUnit.PERCENT);

    TranslatableTextContent shown =
        (TranslatableTextContent) percent.displayValue("90").getContent();

    assertEquals("screen.total-storage.config.value.percent", shown.getKey());
    assertEquals("%s%%", EnglishLang.of(shown.getKey()));
    assertEquals(90, shown.getArgs()[0]);
  }

  @Test
  void aValueThatIsNotANumberIsShownAsItWasWritten() {
    assertEquals(Text.literal("lots"), setting.displayValue("lots"));
  }

  @Test
  void anAbsentValueIsShownAsNothing() {
    assertEquals(Text.empty(), setting.displayValue(null));
  }
}
