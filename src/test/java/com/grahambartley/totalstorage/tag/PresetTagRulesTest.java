package com.grahambartley.totalstorage.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class PresetTagRulesTest {
  @Test
  void anItemMatchingNothingIsFiledUnderTheFallback() {
    assertEquals(Set.of(PresetTag.FALLBACK), PresetTagRules.assign(List.of()));
  }

  @ParameterizedTest
  @EnumSource(value = PresetTag.class, names = "MISCELLANEOUS", mode = EnumSource.Mode.EXCLUDE)
  void anItemMatchingOnePresetIsFiledUnderJustThatPreset(PresetTag preset) {
    assertEquals(Set.of(preset), PresetTagRules.assign(List.of(preset)));
  }

  @Test
  void anItemMayHoldSeveralPresetsAtOnce() {
    Set<PresetTag> assigned =
        PresetTagRules.assign(List.of(PresetTag.WOOD, PresetTag.BUILDING_BLOCKS));

    assertEquals(Set.of(PresetTag.WOOD, PresetTag.BUILDING_BLOCKS), assigned);
  }

  @Test
  void theFallbackIsNeverCombinedWithARealMatch() {
    Set<PresetTag> assigned = PresetTagRules.assign(List.of(PresetTag.FALLBACK, PresetTag.FOOD));

    assertEquals(Set.of(PresetTag.FOOD), assigned);
  }

  @Test
  void aMatchOnTheFallbackAloneStillMeansTheFallback() {
    assertEquals(Set.of(PresetTag.FALLBACK), PresetTagRules.assign(List.of(PresetTag.FALLBACK)));
  }

  @Test
  void aRepeatedMatchIsCountedOnce() {
    Set<PresetTag> assigned =
        PresetTagRules.assign(List.of(PresetTag.STONE, PresetTag.STONE, PresetTag.STONE));

    assertEquals(1, assigned.size());
    assertEquals(Set.of(PresetTag.STONE), assigned);
  }

  @Test
  void aNullMatchIsIgnoredRatherThanThrowing() {
    List<PresetTag> matched = new ArrayList<>();
    matched.add(null);
    matched.add(PresetTag.TOOLS);

    assertEquals(Set.of(PresetTag.TOOLS), PresetTagRules.assign(matched));
  }

  @Test
  void nothingButNullsStillFallsBack() {
    List<PresetTag> matched = new ArrayList<>();
    matched.add(null);

    assertEquals(Set.of(PresetTag.FALLBACK), PresetTagRules.assign(matched));
  }

  @ParameterizedTest
  @MethodSource("shuffledMatches")
  void theAssignedPresetsComeBackInFilterColumnOrderHoweverTheyWereMatched(
      List<PresetTag> matched) {
    List<PresetTag> assigned = List.copyOf(PresetTagRules.assign(matched));

    assertEquals(
        matched.stream().distinct().sorted(java.util.Comparator.naturalOrder()).toList(), assigned);
  }

  static List<List<PresetTag>> shuffledMatches() {
    List<PresetTag> tagged =
        PresetTag.all().stream().filter(preset -> preset != PresetTag.FALLBACK).toList();
    List<List<PresetTag>> rotations = new ArrayList<>();
    for (int offset = 0; offset < tagged.size(); offset++) {
      List<PresetTag> rotated = new ArrayList<>(tagged);
      Collections.rotate(rotated, offset);
      rotations.add(List.copyOf(rotated));
    }
    return rotations;
  }

  @Test
  void theAssignedPresetsCannotBeAddedTo() {
    Set<PresetTag> assigned = PresetTagRules.assign(List.of(PresetTag.FOOD));

    assertThrows(UnsupportedOperationException.class, () -> assigned.add(PresetTag.WOOD));
  }

  @Test
  void everyPresetIsReachableFromSomeMatch() {
    for (PresetTag preset : PresetTag.all()) {
      Set<PresetTag> assigned = PresetTagRules.assign(Arrays.asList(preset));

      assertTrue(
          assigned.contains(preset) || preset == PresetTag.FALLBACK,
          preset + " should be assignable");
    }
  }
}
