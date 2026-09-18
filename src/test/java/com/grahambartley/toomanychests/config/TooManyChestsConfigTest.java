package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TooManyChestsConfigTest {
  @Test
  void defaultsMatchTheDocumentedValues() {
    TooManyChestsConfig defaults = TooManyChestsConfig.defaults();

    assertEquals(16, defaults.maxTerminalRadius());
    assertEquals(TerminalAccessMode.OWNER, defaults.defaultTerminalAccessMode());
    assertEquals(SortOrdering.REGISTRY_ID, defaults.sortOrdering());
    assertTrue(defaults.sortButtonEnabled());
    assertEquals(8, defaults.quickStackRadius());
    assertEquals(false, defaults.depositAllSkipsHotbar());
    assertEquals(90, defaults.nearCapacityWarningPercent());
    assertEquals(Map.of(), defaults.containerEligibilityOverrides());
  }

  @ParameterizedTest
  @CsvSource({"0, 1", "-5, 1", "65, 64", "16, 16", "1, 1", "64, 64"})
  void maxTerminalRadiusIsClampedIntoRange(int given, int expected) {
    assertEquals(expected, TestConfigs.withInts(given, 8, 90, 10, 64).maxTerminalRadius());
  }

  @ParameterizedTest
  @CsvSource({"0, 1", "65, 64", "8, 8"})
  void quickStackRadiusIsClampedIntoRange(int given, int expected) {
    assertEquals(expected, TestConfigs.withInts(16, given, 90, 10, 64).quickStackRadius());
  }

  @ParameterizedTest
  @CsvSource({"-1, 0", "101, 100", "90, 90"})
  void nearCapacityWarningPercentIsClampedIntoRange(int given, int expected) {
    assertEquals(expected, TestConfigs.withInts(16, 8, given, 10, 64).nearCapacityWarningPercent());
  }

  @ParameterizedTest
  @CsvSource({"-1, 0", "500, 200", "10, 10"})
  void expensiveOperationCooldownIsClampedIntoRange(int given, int expected) {
    assertEquals(
        expected, TestConfigs.withInts(16, 8, 90, given, 64).expensiveOperationCooldownTicks());
  }

  @ParameterizedTest
  @CsvSource({"0, 1", "5000, 1024", "64, 64"})
  void movesPerTickBudgetIsClampedIntoRange(int given, int expected) {
    assertEquals(expected, TestConfigs.withInts(16, 8, 90, 10, given).movesPerTickBudget());
  }

  @Test
  void aMissingAccessModeFallsBackToTheDefault() {
    assertEquals(
        TerminalAccessMode.OWNER,
        TestConfigs.withModes(null, SortOrdering.REGISTRY_ID).defaultTerminalAccessMode());
  }

  @Test
  void aMissingSortOrderingFallsBackToTheDefault() {
    assertEquals(
        SortOrdering.REGISTRY_ID,
        TestConfigs.withModes(TerminalAccessMode.OWNER, null).sortOrdering());
  }

  @Test
  void missingEligibilityOverridesBecomeAnEmptyMap() {
    assertEquals(Map.of(), TestConfigs.withOverrides(null).containerEligibilityOverrides());
  }

  @Test
  void eligibilityOverridesCannotBeMutatedThroughTheRecord() {
    Map<String, Boolean> overrides =
        TestConfigs.withOverrides(Map.of("minecraft:furnace", true))
            .containerEligibilityOverrides();

    assertThrows(UnsupportedOperationException.class, () -> overrides.put("minecraft:chest", true));
  }

  @Test
  void eligibilityOverridesAreDetachedFromTheCallersMap() {
    Map<String, Boolean> caller = new HashMap<>();
    caller.put("minecraft:furnace", true);
    TooManyChestsConfig config = TestConfigs.withOverrides(caller);

    caller.put("minecraft:chest", false);

    assertEquals(Map.of("minecraft:furnace", true), config.containerEligibilityOverrides());
  }

  @Test
  void aBareBlockIdIsNormalisedToTheVanillaNamespace() {
    TooManyChestsConfig config = TestConfigs.withOverrides(Map.of("furnace", true));

    assertEquals(Map.of("minecraft:furnace", true), config.containerEligibilityOverrides());
  }

  @Test
  void anUnparseableBlockIdIsDropped() {
    Map<String, Boolean> raw = new HashMap<>();
    raw.put("Not A Block!", true);
    raw.put("minecraft:furnace", true);

    assertEquals(
        Map.of("minecraft:furnace", true),
        TestConfigs.withOverrides(raw).containerEligibilityOverrides());
  }

  @Test
  void anOverrideWithNoValueIsDropped() {
    Map<String, Boolean> raw = new HashMap<>();
    raw.put("minecraft:furnace", null);

    assertEquals(Map.of(), TestConfigs.withOverrides(raw).containerEligibilityOverrides());
  }

  @Test
  void anOperatorOverrideIsReadableByBlockId() {
    TooManyChestsConfig config = TestConfigs.withOverrides(Map.of("minecraft:furnace", true));

    assertEquals(
        Optional.of(true),
        config.containerEligibilityOverrideFor(Identifier.of("minecraft", "furnace")));
  }

  @Test
  void aBlockWithNoOverrideReadsAsAbsent() {
    TooManyChestsConfig config = TestConfigs.withOverrides(Map.of("minecraft:furnace", true));

    assertTrue(
        config.containerEligibilityOverrideFor(Identifier.of("minecraft", "chest")).isEmpty());
  }

  @Test
  void aNullBlockIdReadsAsAbsent() {
    assertTrue(TooManyChestsConfig.defaults().containerEligibilityOverrideFor(null).isEmpty());
  }

  @Test
  void replacingTheEligibilityOverridesLeavesEveryOtherSettingAlone() {
    TooManyChestsConfig original = TestConfigs.everythingNonDefault();

    TooManyChestsConfig updated =
        original.withContainerEligibilityOverrides(Map.of("minecraft:hopper", true));

    assertEquals(Map.of("minecraft:hopper", true), updated.containerEligibilityOverrides());
    assertEquals(original.maxTerminalRadius(), updated.maxTerminalRadius());
    assertEquals(original.defaultTerminalAccessMode(), updated.defaultTerminalAccessMode());
    assertEquals(original.sortOrdering(), updated.sortOrdering());
    assertEquals(original.sortButtonEnabled(), updated.sortButtonEnabled());
    assertEquals(original.quickStackRadius(), updated.quickStackRadius());
    assertEquals(original.depositAllSkipsHotbar(), updated.depositAllSkipsHotbar());
    assertEquals(original.nearCapacityWarningPercent(), updated.nearCapacityWarningPercent());
    assertEquals(
        original.expensiveOperationCooldownTicks(), updated.expensiveOperationCooldownTicks());
    assertEquals(original.movesPerTickBudget(), updated.movesPerTickBudget());
  }

  @Test
  void replacingTheEligibilityOverridesStillNormalisesBlockIds() {
    TooManyChestsConfig updated =
        TooManyChestsConfig.defaults().withContainerEligibilityOverrides(Map.of("hopper", true));

    assertEquals(Map.of("minecraft:hopper", true), updated.containerEligibilityOverrides());
  }

  @Test
  void clearingTheEligibilityOverridesLeavesNoneBehind() {
    TooManyChestsConfig withOverrides =
        TestConfigs.withOverrides(Map.of("minecraft:furnace", true));

    assertEquals(
        Map.of(),
        withOverrides.withContainerEligibilityOverrides(Map.of()).containerEligibilityOverrides());
  }
}
