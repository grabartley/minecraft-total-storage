package com.grahambartley.toomanychests.filter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FuzzyNameMatcherTest {
  @ParameterizedTest
  @CsvSource({
    "Dark Oak Chest,dch",
    "Dark Oak Chest,dark",
    "Dark Oak Chest,chest",
    "Dark Oak Chest,dark chest",
    "Dark Oak Chest,DARK",
    "Cobblestone,cbl",
    "Cobblestone,cobblestone",
    "minecraft:cobblestone,cobble"
  })
  void matchesAcceptsAbbreviationsAndSubstrings(String candidate, String term) {
    assertTrue(FuzzyNameMatcher.matches(candidate, term));
  }

  @ParameterizedTest
  @CsvSource({
    "Dark Oak Chest,dcx",
    "Dark Oak Chest,chestd",
    "Cobblestone,diamond",
    "Cobblestone,cobblestones"
  })
  void matchesRejectsCharactersMissingOrOutOfOrder(String candidate, String term) {
    assertFalse(FuzzyNameMatcher.matches(candidate, term));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void anEmptyTermMatchesAnything(String term) {
    assertTrue(FuzzyNameMatcher.matches("Cobblestone", term));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   "})
  void anEmptyCandidateMatchesNoRealTerm(String candidate) {
    assertFalse(FuzzyNameMatcher.matches(candidate, "cobble"));
  }

  @Test
  void anEmptyCandidateStillMatchesAnEmptyTerm() {
    assertTrue(FuzzyNameMatcher.matches(null, null));
  }

  @Test
  void matchingIsOrderedSoRepeatedCharactersMustAllAppear() {
    assertTrue(FuzzyNameMatcher.matches("Cobblestone", "bb"));
    assertFalse(FuzzyNameMatcher.matches("Cobblestone", "bbb"));
  }
}
