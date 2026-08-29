package com.grahambartley.toomanychests.sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.tag.PresetTag;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SortCandidateTest {
  private static SortCandidate withCategories(PresetTag... categories) {
    return new SortCandidate(
        categories == null ? null : Set.of(categories), "minecraft:stone", "", "Stone", 1L, 64);
  }

  @Test
  void anItemFiledUnderNoPresetGroupsUnderTheFallback() {
    assertEquals(Set.of(PresetTag.FALLBACK), withCategories().categories());
  }

  @Test
  void nullCategoriesGroupUnderTheFallback() {
    assertEquals(PresetTag.FALLBACK, withCategories((PresetTag[]) null).primaryCategory());
  }

  @Test
  void theEarliestPresetInFilterColumnOrderIsThePrimaryCategory() {
    SortCandidate candidate = withCategories(PresetTag.FOOD, PresetTag.WOOD, PresetTag.TOOLS);

    assertEquals(PresetTag.WOOD, candidate.primaryCategory());
  }

  @ParameterizedTest
  @MethodSource("presetPairs")
  void thePrimaryCategoryDoesNotDependOnTheOrderThePresetsArrivedIn(
      PresetTag first, PresetTag second) {
    assertEquals(
        withCategories(first, second).primaryCategory(),
        withCategories(second, first).primaryCategory());
  }

  static Object[][] presetPairs() {
    return PresetTag.all().stream()
        .flatMap(first -> PresetTag.all().stream().map(second -> new Object[] {first, second}))
        .filter(pair -> pair[0] != pair[1])
        .toArray(Object[][]::new);
  }

  @Test
  void thePrimaryCategoryIsTheEarliestOfEveryPresetAtOnce() {
    SortCandidate candidate =
        withCategories(EnumSet.allOf(PresetTag.class).toArray(PresetTag[]::new));

    assertEquals(PresetTag.all().get(0), candidate.primaryCategory());
  }

  @ParameterizedTest
  @CsvSource({"64, 64, true", "65, 64, true", "63, 64, false", "1, 1, true", "0, 64, false"})
  void aHoldingIsFullOnceItReachesTheStackLimit(long count, int maxCount, boolean full) {
    SortCandidate candidate =
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", "", "Stone", count, maxCount);

    assertEquals(full, candidate.isFullStack());
  }

  @Test
  void aHoldingOfAnUnstackableItemIsFullAtOne() {
    SortCandidate candidate =
        TestCandidates.of(PresetTag.TOOLS, "minecraft:iron_pickaxe", "", "Iron Pickaxe", 1L, 1);

    assertTrue(candidate.isFullStack());
  }

  @Test
  void anEmptyHoldingIsNeverFull() {
    SortCandidate candidate =
        TestCandidates.of(PresetTag.TOOLS, "minecraft:iron_pickaxe", "", "Iron Pickaxe", 0L, 1);

    assertFalse(candidate.isFullStack());
  }

  @ParameterizedTest
  @ValueSource(strings = {"  MINECRAFT:Stone  ", "minecraft:stone", "MINECRAFT:STONE"})
  void identifiersAreFoldedSoCasingNeverChangesTheOrder(String identifier) {
    SortCandidate candidate = TestCandidates.of(PresetTag.STONE, identifier, "", "Stone", 1L, 64);

    assertEquals("minecraft:stone", candidate.identifier());
  }

  @Test
  void componentKeysAndDisplayNamesAreFoldedTheSameWay() {
    SortCandidate candidate =
        TestCandidates.of(
            PresetTag.WEAPONS_AND_ARMOUR,
            "minecraft:diamond_sword",
            "  Damage=5 ",
            " Sword ",
            1L,
            1);

    assertEquals("damage=5", candidate.componentsKey());
    assertEquals("sword", candidate.displayName());
  }

  @ParameterizedTest
  @NullAndEmptySource
  void anAbsentComponentKeyBecomesTheEmptyKey(String componentsKey) {
    SortCandidate candidate =
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", componentsKey, "Stone", 1L, 64);

    assertEquals("", candidate.componentsKey());
  }

  @Test
  void aNullIdentifierIsRejected() {
    assertThrows(
        NullPointerException.class,
        () -> new SortCandidate(Set.of(PresetTag.STONE), null, "", "Stone", 1L, 64));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void aBlankIdentifierIsRejected(String identifier) {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SortCandidate(Set.of(PresetTag.STONE), identifier, "", "Stone", 1L, 64));
  }

  @Test
  void aNegativeCountIsRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SortCandidate(Set.of(PresetTag.STONE), "minecraft:stone", "", "Stone", -1L, 64));
  }

  @ParameterizedTest
  @ValueSource(ints = {0, -1})
  void aStackLimitBelowOneIsRejected(int maxCount) {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new SortCandidate(
                Set.of(PresetTag.STONE), "minecraft:stone", "", "Stone", 1L, maxCount));
  }

  @Test
  void theCategoriesAreNotAffectedByLaterChangesToTheSetPassedIn() {
    EnumSet<PresetTag> mutable = EnumSet.of(PresetTag.WOOD);
    SortCandidate candidate =
        new SortCandidate(mutable, "minecraft:oak_planks", "", "Oak Planks", 1L, 64);
    mutable.add(PresetTag.BUILDING_BLOCKS);

    assertEquals(Set.of(PresetTag.WOOD), candidate.categories());
  }

  @Test
  void candidatesDescribingTheSameHoldingAreEqual() {
    assertEquals(
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", 5L),
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", 5L));
  }

  @Test
  void everyPresetCanBeAPrimaryCategoryOnItsOwn() {
    assertEquals(
        PresetTag.all(),
        Arrays.stream(PresetTag.values())
            .map(preset -> withCategories(preset).primaryCategory())
            .toList());
  }

  @Test
  void changingTheQuantityKeepsEverythingElseAboutTheHolding() {
    SortCandidate original = TestCandidates.of(PresetTag.STONE, "minecraft:stone", 5L);

    SortCandidate restacked = original.withCount(40L);

    assertEquals(40L, restacked.count());
    assertEquals(original.categories(), restacked.categories());
    assertEquals(original.identifier(), restacked.identifier());
    assertEquals(original.componentsKey(), restacked.componentsKey());
    assertEquals(original.displayName(), restacked.displayName());
    assertEquals(original.maxCount(), restacked.maxCount());
  }

  @Test
  void changingTheQuantityToWhatItAlreadyIsChangesNothing() {
    SortCandidate original = TestCandidates.of(PresetTag.STONE, "minecraft:stone", 5L);

    assertSame(original, original.withCount(5L));
  }

  @Test
  void aQuantityBelowZeroIsStillRejectedWhenChangingIt() {
    SortCandidate original = TestCandidates.of(PresetTag.STONE, "minecraft:stone", 5L);

    assertThrows(IllegalArgumentException.class, () -> original.withCount(-1L));
  }

  @ParameterizedTest
  @CsvSource({"64,16,16", "64,1,1", "16,64,16", "64,64,64", "16,16,16"})
  void cappingLowersTheStackLimitButNeverRaisesIt(int maxCount, int capacity, int expected) {
    SortCandidate candidate =
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", "", "Stone", 5L, maxCount);

    assertEquals(expected, candidate.cappedAt(capacity).maxCount());
  }

  @Test
  void cappingKeepsTheQuantityAndEverythingElse() {
    SortCandidate original =
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", "", "Stone", 40L, 64);

    SortCandidate capped = original.cappedAt(16);

    assertEquals(40L, capped.count());
    assertEquals(original.identifier(), capped.identifier());
    assertEquals(original.categories(), capped.categories());
  }

  @Test
  void cappingAtOrAboveTheStackLimitChangesNothing() {
    SortCandidate original =
        TestCandidates.of(PresetTag.STONE, "minecraft:stone", "", "Stone", 5L, 64);

    assertSame(original, original.cappedAt(64));
    assertSame(original, original.cappedAt(999));
  }
}
