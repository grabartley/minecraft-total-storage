package com.grahambartley.toomanychests.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SearchOrderingTest {
  private static final Map<String, FilterCandidate> CANDIDATES =
      Map.of(
          "chest",
          new FilterCandidate("minecraft:chest", "Chest", Set.of("storage")),
          "chest_minecart",
          new FilterCandidate("minecraft:chest_minecart", "Minecart with Chest", Set.of("storage")),
          "ender_chest",
          new FilterCandidate("minecraft:ender_chest", "Ender Chest", Set.of("storage")),
          "chestplate",
          new FilterCandidate("minecraft:iron_chestplate", "Iron Chestplate", Set.of("armour")),
          "coarse_dirt",
          new FilterCandidate("minecraft:coarse_dirt", "Coarse Dirt", Set.of("nature")));

  /** Index order, which is by identifier, so what ranking changed is visible against it. */
  private static final List<String> IN_INDEX_ORDER =
      List.of("chest", "chest_minecart", "chestplate", "coarse_dirt", "ender_chest");

  private static List<String> rankedBy(String raw) {
    return SearchOrdering.rank(IN_INDEX_ORDER, CANDIDATES::get, SearchQueryParser.parse(raw));
  }

  @Test
  void theClosestAnswerComesFirstAndTheLoosestLast() {
    assertEquals(
        List.of("chest", "chest_minecart", "chestplate", "ender_chest", "coarse_dirt"),
        rankedBy("chest"));
  }

  @ParameterizedTest
  @CsvSource({"chest,chest", "ender,ender_chest", "coa,coarse_dirt"})
  void whatTheQueryMostObviouslyNamesIsFirst(String raw, String expected) {
    assertEquals(expected, rankedBy(raw).getFirst());
  }

  /** Both hold the term whole and unbroken, so nothing but the index has an opinion on them. */
  @Test
  void rowsOfEqualStrengthKeepTheOrderTheIndexGaveThem() {
    final List<String> ranked = rankedBy("chest");
    assertTrue(ranked.indexOf("chestplate") < ranked.indexOf("ender_chest"));
  }

  @Test
  void aQueryWithNoTextToRankLeavesTheIndexOrderAlone() {
    assertSame(IN_INDEX_ORDER, rankedBy(""));
    assertSame(IN_INDEX_ORDER, rankedBy("#storage @minecraft"));
  }

  @Test
  void aSingleRowIsAlreadyInOrder() {
    final List<String> one = List.of("ender_chest");
    assertSame(one, SearchOrdering.rank(one, CANDIDATES::get, SearchQueryParser.parse("chest")));
  }

  @Test
  void rankingKeepsEveryRowItWasGiven() {
    assertEquals(Set.copyOf(IN_INDEX_ORDER), Set.copyOf(rankedBy("chest")));
    assertEquals(IN_INDEX_ORDER.size(), rankedBy("chest").size());
  }

  @Test
  void nothingIsOrderedWithoutSomethingToOrder() {
    final Function<String, FilterCandidate> toCandidate = CANDIDATES::get;
    assertThrows(
        NullPointerException.class,
        () -> SearchOrdering.rank(null, toCandidate, SearchQuery.EMPTY));
    assertThrows(
        NullPointerException.class,
        () -> SearchOrdering.rank(IN_INDEX_ORDER, null, SearchQuery.EMPTY));
    assertThrows(
        NullPointerException.class, () -> SearchOrdering.rank(IN_INDEX_ORDER, toCandidate, null));
  }
}
