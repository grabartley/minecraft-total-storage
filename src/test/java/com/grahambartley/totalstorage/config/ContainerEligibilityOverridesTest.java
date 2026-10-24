package com.grahambartley.totalstorage.config;

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
    TotalStorageConfig updated =
        ContainerEligibilityOverrides.set(TotalStorageConfig.defaults(), "minecraft:furnace", true)
            .orElseThrow();

    assertEquals(Map.of("minecraft:furnace", true), updated.containerEligibilityOverrides());
    assertEquals(
        Optional.of(true),
        updated.containerEligibilityOverrideFor(Identifier.of("minecraft", "furnace")));
  }

  @Test
  void forcingABlockIneligibleRecordsIt() {
    TotalStorageConfig updated =
        ContainerEligibilityOverrides.set(TotalStorageConfig.defaults(), "minecraft:barrel", false)
            .orElseThrow();

    assertEquals(
        Optional.of(false),
        updated.containerEligibilityOverrideFor(Identifier.of("minecraft", "barrel")));
  }

  @Test
  void anUnqualifiedBlockNameIsReadAsVanilla() {
    TotalStorageConfig updated =
        ContainerEligibilityOverrides.set(TotalStorageConfig.defaults(), "barrel", false)
            .orElseThrow();

    assertEquals(Map.of("minecraft:barrel", false), updated.containerEligibilityOverrides());
  }

  @Test
  void forcingABlockAgainReplacesTheEarlierVerdict() {
    TotalStorageConfig once =
        ContainerEligibilityOverrides.set(TotalStorageConfig.defaults(), "minecraft:barrel", true)
            .orElseThrow();

    TotalStorageConfig twice =
        ContainerEligibilityOverrides.set(once, "minecraft:barrel", false).orElseThrow();

    assertEquals(Map.of("minecraft:barrel", false), twice.containerEligibilityOverrides());
  }

  @Test
  void forcingOneBlockLeavesTheOthersForced() {
    TotalStorageConfig existing =
        TestConfigs.withOverrides(Map.of("minecraft:furnace", true, "minecraft:barrel", false));

    TotalStorageConfig updated =
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
        ContainerEligibilityOverrides.set(TotalStorageConfig.defaults(), blockId, true));
  }

  @ParameterizedTest
  @NullSource
  void anAbsentIdIsRefused(String blockId) {
    assertEquals(
        Optional.empty(),
        ContainerEligibilityOverrides.set(TotalStorageConfig.defaults(), blockId, true));
  }

  @Test
  void clearingRemovesOnlyTheBlockNamed() {
    TotalStorageConfig existing =
        TestConfigs.withOverrides(Map.of("minecraft:furnace", true, "minecraft:barrel", false));

    TotalStorageConfig updated =
        ContainerEligibilityOverrides.clear(existing, "minecraft:furnace").orElseThrow();

    assertEquals(Map.of("minecraft:barrel", false), updated.containerEligibilityOverrides());
  }

  @Test
  void clearingABlockNothingWasForcingChangesNothing() {
    TotalStorageConfig existing = TestConfigs.withOverrides(Map.of("minecraft:barrel", false));

    assertEquals(
        Optional.empty(), ContainerEligibilityOverrides.clear(existing, "minecraft:furnace"));
  }

  @Test
  void clearingEveryOverrideLeavesTheRulesToDecide() {
    TotalStorageConfig existing =
        TestConfigs.withOverrides(Map.of("minecraft:furnace", true, "minecraft:barrel", false));

    TotalStorageConfig updated = ContainerEligibilityOverrides.clearAll(existing);

    assertTrue(updated.containerEligibilityOverrides().isEmpty());
  }

  @Test
  void editingOverridesLeavesEveryOtherSettingAlone() {
    TotalStorageConfig original = TestConfigs.everythingNonDefault();

    TotalStorageConfig updated =
        ContainerEligibilityOverrides.set(original, "minecraft:hopper", true).orElseThrow();

    assertEquals(
        ConfigBuilder.from(original)
            .containerEligibilityOverrides(updated.containerEligibilityOverrides())
            .build(),
        updated);
  }

  @Test
  void overridesKeepTheOrderTheyWereAddedIn() {
    TotalStorageConfig updated =
        ContainerEligibilityOverrides.set(
                ContainerEligibilityOverrides.set(
                        TotalStorageConfig.defaults(), "minecraft:furnace", true)
                    .orElseThrow(),
                "minecraft:barrel",
                false)
            .orElseThrow();

    assertEquals(
        List.of("minecraft:furnace", "minecraft:barrel"),
        List.copyOf(updated.containerEligibilityOverrides().keySet()));
  }
}
