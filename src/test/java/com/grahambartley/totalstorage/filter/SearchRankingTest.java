package com.grahambartley.totalstorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SearchRankingTest {
  private static final FilterCandidate CHEST =
      new FilterCandidate("minecraft:chest", "Chest", Set.of("storage"));

  private static MatchStrength rankOf(FilterCandidate candidate, String raw) {
    return SearchRanking.of(candidate, SearchQueryParser.parse(raw), true);
  }

  @ParameterizedTest
  @CsvSource({
    "chest,true,EXACT",
    "che,true,PREFIX",
    "hes,true,SUBSTRING",
    "cst,true,FUZZY",
    "minecraft:chest,true,EXACT",
    "minecraft:ch,true,PREFIX",
    "chest,false,EXACT",
    "che,false,PREFIX",
    "hes,false,SUBSTRING",
    "cst,false,NONE",
    "minecraft:chest,false,EXACT",
    "minecraft:ch,false,PREFIX",
    "minecraft:cst,true,NONE"
  })
  void aQueryIsRankedByHowCloselyItNamesTheItem(String raw, boolean fuzzy, MatchStrength expected) {
    assertEquals(expected, SearchRanking.of(CHEST, SearchQueryParser.parse(raw), fuzzy));
  }

  @ParameterizedTest
  @CsvSource({"Steak,stk,true,FUZZY", "Steak,stk,false,NONE", "Steak,ckdbf,true,FUZZY"})
  void fuzzyMatchingReachesBothTheDisplayNameAndTheIdentifierPathOnlyWhenAskedFor(
      String displayName, String term, boolean fuzzy, MatchStrength expected) {
    final FilterCandidate steak =
        new FilterCandidate("minecraft:cooked_beef", displayName, Set.of("food"));
    assertEquals(expected, SearchRanking.ofTerm(steak, term, fuzzy));
  }

  @Test
  void theDisplayNameAndTheIdentifierPathAreBothRankedAndTheBetterOneWins() {
    final FilterCandidate steak =
        new FilterCandidate("minecraft:cooked_beef", "Steak", Set.of("food"));
    assertEquals(MatchStrength.EXACT, rankOf(steak, "steak"));
    assertEquals(MatchStrength.PREFIX, rankOf(steak, "cooked"));
  }

  @Test
  void theNamespaceIsNotSomethingABareTermCanRankAgainst() {
    assertEquals(MatchStrength.NONE, rankOf(CHEST, "minecraft"));
  }

  @Test
  void aQueryOfSeveralTermsIsOnlyAsGoodAsItsWeakestOne() {
    final FilterCandidate darkOakChest =
        new FilterCandidate("minecraft:dark_oak_chest", "Dark Oak Chest", Set.of("wood"));
    assertEquals(MatchStrength.PREFIX, rankOf(darkOakChest, "dark"));
    assertEquals(MatchStrength.FUZZY, rankOf(darkOakChest, "dark dkchst"));
  }

  @Test
  void tagsAndModIdentifiersAreNotRankedAtAll() {
    assertEquals(MatchStrength.EXACT, rankOf(CHEST, "#storage @minecraft"));
    assertEquals(MatchStrength.EXACT, SearchRanking.of(CHEST, SearchQuery.EMPTY, false));
  }

  @Test
  void aTagOrModTermBesideTextLeavesTheTextDecidingTheRank() {
    assertEquals(MatchStrength.PREFIX, rankOf(CHEST, "@minecraft che"));
  }

  @Test
  void aTermIsRankedAgainstOneCandidateAtATime() {
    assertEquals(MatchStrength.PREFIX, SearchRanking.ofTerm(CHEST, "che", false));
    assertEquals(MatchStrength.NONE, SearchRanking.ofTerm(CHEST, "diamond", true));
  }

  @Test
  void nothingIsRankedWithoutSomethingToRank() {
    assertThrows(NullPointerException.class, () -> SearchRanking.of(null, SearchQuery.EMPTY, true));
    assertThrows(NullPointerException.class, () -> SearchRanking.of(CHEST, null, true));
    assertThrows(NullPointerException.class, () -> SearchRanking.ofTerm(null, "chest", true));
  }

  @Test
  void aRowKeptOutByAGateIsNotGradedByItsText() {
    final ItemFilter gated = ItemFilter.ofSearch("#storage @create ch");
    assertEquals(MatchStrength.PREFIX, rankOf(CHEST, "ch"));
    assertEquals(MatchStrength.NONE, FilterEvaluator.strengthOf(CHEST, gated));
    assertEquals(
        MatchStrength.PREFIX,
        FilterEvaluator.strengthOf(CHEST, ItemFilter.ofSearch("#storage @minecraft ch")));
  }
}
