package com.grahambartley.totalstorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class TagCombinationModeTest {
  private static final Set<String> STONE_TOOL = Set.of("stone", "tools", "building_blocks");

  private static Set<String> tags(String joined) {
    return joined.isEmpty() ? Set.of() : Set.of(joined.split("\\|"));
  }

  @ParameterizedTest
  @CsvSource({
    "stone,true",
    "stone|tools,true",
    "stone|tools|building_blocks,true",
    "stone|food,false",
    "food,false",
    "food|brewing,false"
  })
  void andRequiresEveryActiveTag(String active, boolean expected) {
    assertEquals(expected, TagCombinationMode.AND.matches(STONE_TOOL, tags(active)));
  }

  @ParameterizedTest
  @CsvSource({
    "stone,true",
    "stone|food,true",
    "food|tools,true",
    "food,false",
    "food|brewing,false"
  })
  void orRequiresOnlyOneActiveTag(String active, boolean expected) {
    assertEquals(expected, TagCombinationMode.OR.matches(STONE_TOOL, tags(active)));
  }

  @ParameterizedTest
  @EnumSource(TagCombinationMode.class)
  void noActiveTagsMatchesEverything(TagCombinationMode mode) {
    assertTrue(mode.matches(STONE_TOOL, Set.of()));
    assertTrue(mode.matches(Set.of(), Set.of()));
    assertTrue(mode.matches(STONE_TOOL, null));
  }

  @ParameterizedTest
  @EnumSource(TagCombinationMode.class)
  void anItemWithNoTagsMatchesNoActiveFilter(TagCombinationMode mode) {
    assertFalse(mode.matches(Set.of(), Set.of("stone")));
    assertFalse(mode.matches(null, Set.of("stone")));
  }

  @Test
  void andIsTheDefaultSoEachExtraFilterNarrows() {
    assertEquals(TagCombinationMode.AND, TagCombinationMode.DEFAULT);
  }
}
