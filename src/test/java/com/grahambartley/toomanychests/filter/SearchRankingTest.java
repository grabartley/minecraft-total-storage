package com.grahambartley.toomanychests.filter;

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
    return SearchRanking.of(candidate, SearchQueryParser.parse(raw));
  }

  @ParameterizedTest
  @CsvSource({
    "chest,EXACT",
    "che,PREFIX",
    "hes,SUBSTRING",
    "cst,FUZZY",
    "minecraft:chest,EXACT",
    "minecraft:ch,PREFIX"
  })
  void aQueryIsRankedByHowCloselyItNamesTheItem(String raw, MatchStrength expected) {
    assertEquals(expected, rankOf(CHEST, raw));
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
    assertEquals(MatchStrength.EXACT, SearchRanking.of(CHEST, SearchQuery.EMPTY));
  }

  @Test
  void aTagOrModTermBesideTextLeavesTheTextDecidingTheRank() {
    assertEquals(MatchStrength.PREFIX, rankOf(CHEST, "@minecraft che"));
  }

  @Test
  void aTermIsRankedAgainstOneCandidateAtATime() {
    assertEquals(MatchStrength.PREFIX, SearchRanking.ofTerm(CHEST, "che"));
    assertEquals(MatchStrength.NONE, SearchRanking.ofTerm(CHEST, "diamond"));
  }

  @Test
  void nothingIsRankedWithoutSomethingToRank() {
    assertThrows(NullPointerException.class, () -> SearchRanking.of(null, SearchQuery.EMPTY));
    assertThrows(NullPointerException.class, () -> SearchRanking.of(CHEST, null));
    assertThrows(NullPointerException.class, () -> SearchRanking.ofTerm(null, "chest"));
  }

  @Test
  void everyAcceptedRowCanBeRanked() {
    final SearchQuery query = SearchQueryParser.parse("ch");
    final ItemFilter filter = ItemFilter.ofSearch("ch");
    for (final FilterCandidate candidate : TestCandidates.all()) {
      final boolean accepted = FilterEvaluator.accepts(candidate, filter);
      assertEquals(
          accepted,
          SearchRanking.of(candidate, query).isMatch(),
          candidate.identifier() + " must be ranked exactly when it is accepted");
    }
  }
}
