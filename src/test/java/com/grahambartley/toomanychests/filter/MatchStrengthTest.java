package com.grahambartley.toomanychests.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

class MatchStrengthTest {
  @ParameterizedTest
  @CsvSource({
    "Cobblestone,cobblestone,EXACT",
    "Cobblestone,COBBLESTONE,EXACT",
    "Dark Oak Chest,dark,PREFIX",
    "Dark Oak Chest,dark oak,PREFIX",
    "Dark Oak Chest,oak chest,SUBSTRING",
    "Dark Oak Chest,chest,SUBSTRING",
    "Dark Oak Chest,dch,FUZZY",
    "Dark Oak Chest,dkchst,FUZZY",
    "Cobblestone,cbl,FUZZY",
    "Dark Oak Chest,chestd,NONE",
    "Cobblestone,diamond,NONE",
    "Cobblestone,cobblestones,NONE"
  })
  void ofScoresATermFromTheWholeStringDownToASubsequence(
      String candidate, String term, MatchStrength expected) {
    assertEquals(expected, MatchStrength.of(candidate, term));
  }

  @ParameterizedTest
  @CsvSource({
    "minecraft:cobblestone,minecraft:cobblestone,EXACT",
    "minecraft:cobblestone,minecraft:,PREFIX",
    "minecraft:cobblestone,ft:cobble,SUBSTRING",
    "minecraft:cobblestone,mc:cbl,NONE",
    "minecraft:cobblestone,minecraft:cobbles,PREFIX",
    "minecraft:cobblestone,minecraft:cobblez,NONE"
  })
  void literalStopsShortOfASubsequence(String candidate, String term, MatchStrength expected) {
    assertEquals(expected, MatchStrength.literal(candidate, term));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void aTermThatNarrowsNothingIsAnsweredExactlyByAnything(String term) {
    assertEquals(MatchStrength.EXACT, MatchStrength.of("Cobblestone", term));
    assertEquals(MatchStrength.EXACT, MatchStrength.of("", term));
  }

  @Test
  void casingAndSurroundingSpaceAreNotWhatTellsTwoAnswersApart() {
    assertEquals(MatchStrength.EXACT, MatchStrength.of("Cobblestone", "  COBBLESTONE  "));
    assertEquals(MatchStrength.PREFIX, MatchStrength.of("  Dark Oak Chest ", "DARK"));
  }

  @Test
  void aTermIsNotFoundInTextThatIsNotThere() {
    assertEquals(MatchStrength.NONE, MatchStrength.of("", "cobblestone"));
    assertEquals(MatchStrength.NONE, MatchStrength.of(null, "cobblestone"));
  }

  @Test
  void aNullTermNarrowsNothing() {
    assertEquals(MatchStrength.EXACT, MatchStrength.of("Cobblestone", null));
  }

  @ParameterizedTest
  @CsvSource({
    "EXACT,PREFIX,EXACT",
    "PREFIX,EXACT,EXACT",
    "SUBSTRING,FUZZY,SUBSTRING",
    "NONE,FUZZY,FUZZY",
    "NONE,NONE,NONE",
    "EXACT,EXACT,EXACT"
  })
  void bestTakesTheStrongerOfTwoAnswers(
      MatchStrength first, MatchStrength second, MatchStrength expected) {
    assertEquals(expected, MatchStrength.best(first, second));
  }

  @ParameterizedTest
  @EnumSource(
      value = MatchStrength.class,
      names = {"EXACT", "PREFIX", "SUBSTRING", "FUZZY"})
  void everyStrengthButNoneIsAMatch(MatchStrength strength) {
    assertTrue(strength.isMatch());
  }

  @Test
  void noneIsNotAMatch() {
    assertFalse(MatchStrength.NONE.isMatch());
  }

  @Test
  void theStrongestAnswerSortsFirst() {
    assertTrue(MatchStrength.EXACT.compareTo(MatchStrength.PREFIX) < 0);
    assertTrue(MatchStrength.PREFIX.compareTo(MatchStrength.SUBSTRING) < 0);
    assertTrue(MatchStrength.SUBSTRING.compareTo(MatchStrength.FUZZY) < 0);
    assertTrue(MatchStrength.FUZZY.compareTo(MatchStrength.NONE) < 0);
  }
}
