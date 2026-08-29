package com.grahambartley.toomanychests.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerEligibilityOverridesTest {
  @Test
  void forcingABlockEligibleRecordsIt() {
    TooManyChestsConfig updated =
        ContainerEligibilityOverrides.set(TooManyChestsConfig.defaults(), "minecraft:furnace", true)
            .orElseThrow();

    assertEquals(Map.of("minecraft:furnace", true), updated.containerEligibilityOverrides());
    assertEquals(
        Optional.of(true),
        updated.containerEligibilityOverrideFor(Identifier.of("minecraft", "furnace")));
  }

  @Test
  void forcingABlockIneligibleRecordsIt() {
    TooManyChestsConfig updated =
        ContainerEligibilityOverrides.set(TooManyChestsConfig.defaults(), "minecraft:barrel", false)
            .orElseThrow();

    assertEquals(
        Optional.of(false),
        updated.containerEligibilityOverrideFor(Identifier.of("minecraft", "barrel")));
  }

  @Test
  void anUnqualifiedBlockNameIsReadAsVanilla() {
    TooManyChestsConfig updated =
        ContainerEligibilityOverrides.set(TooManyChestsConfig.defaults(), "barrel", false)
            .orElseThrow();

    assertEquals(Map.of("minecraft:barrel", false), updated.containerEligibilityOverrides());
  }

  @Test
  void forcingABlockAgainReplacesTheEarlierVerdict() {
    TooManyChestsConfig once =
        ContainerEligibilityOverrides.set(TooManyChestsConfig.defaults(), "minecraft:barrel", true)
            .orElseThrow();

    TooManyChestsConfig twice =
        ContainerEligibilityOverrides.set(once, "minecraft:barrel", false).orElseThrow();

    assertEquals(Map.of("minecraft:barrel", false), twice.containerEligibilityOverrides());
  }

  @Test
  void forcingOneBlockLeavesTheOthersForced() {
    TooManyChestsConfig existing =
        TestConfigs.withOverrides(Map.of("minecraft:furnace", true, "minecraft:barrel", false));

    TooManyChestsConfig updated =
        ContainerEligibilityOverrides.set(existing, "minecraft:hopper", true).orElseThrow();

    assertEquals(
        Map.of("minecraft:furnace", true, "minecraft:barrel", false, "minecraft:hopper", true),
        updated.containerEligibilityOverrides());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Not An Id", "minecraft:FURNACE", "", "  ", "mine craft:furnace"})
  void anIdThatNamesNoBlockIsRefusedRatherThanStored(String blockId) {
    assertEquals(
        Optional.empty(),
        ContainerEligibilityOverrides.set(TooManyChestsConfig.defaults(), blockId, true));
  }

  @ParameterizedTest
  @NullSource
  void anAbsentIdIsRefused(String blockId) {
    assertEquals(
        Optional.empty(),
        ContainerEligibilityOverrides.set(TooManyChestsConfig.defaults(), blockId, true));
  }

  @Test
  void clearingRemovesOnlyTheBlockNamed() {
    TooManyChestsConfig existing =
        TestConfigs.withOverrides(Map.of("minecraft:furnace", true, "minecraft:barrel", false));

    TooManyChestsConfig updated =
        ContainerEligibilityOverrides.clear(existing, "minecraft:furnace").orElseThrow();

    assertEquals(Map.of("minecraft:barrel", false), updated.containerEligibilityOverrides());
  }

  @Test
  void clearingABlockNothingWasForcingChangesNothing() {
    TooManyChestsConfig existing = TestConfigs.withOverrides(Map.of("minecraft:barrel", false));

    assertEquals(
        Optional.empty(), ContainerEligibilityOverrides.clear(existing, "minecraft:furnace"));
  }

  @Test
  void clearingEveryOverrideLeavesTheRulesToDecide() {
    TooManyChestsConfig existing =
        TestConfigs.withOverrides(Map.of("minecraft:furnace", true, "minecraft:barrel", false));

    TooManyChestsConfig updated = ContainerEligibilityOverrides.clearAll(existing);

    assertTrue(updated.containerEligibilityOverrides().isEmpty());
  }

  @Test
  void editingOverridesLeavesEveryOtherSettingAlone() {
    TooManyChestsConfig original = TestConfigs.everythingNonDefault();

    TooManyChestsConfig updated =
        ContainerEligibilityOverrides.set(original, "minecraft:hopper", true).orElseThrow();

    assertEquals(
        ConfigBuilder.from(original)
            .containerEligibilityOverrides(updated.containerEligibilityOverrides())
            .build(),
        updated);
  }

  @Test
  void overridesKeepTheOrderTheyWereAddedIn() {
    TooManyChestsConfig updated =
        ContainerEligibilityOverrides.set(
                ContainerEligibilityOverrides.set(
                        TooManyChestsConfig.defaults(), "minecraft:furnace", true)
                    .orElseThrow(),
                "minecraft:barrel",
                false)
            .orElseThrow();

    assertEquals(
        List.of("minecraft:furnace", "minecraft:barrel"),
        List.copyOf(updated.containerEligibilityOverrides().keySet()));
  }
}
