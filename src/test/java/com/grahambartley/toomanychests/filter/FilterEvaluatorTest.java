package com.grahambartley.toomanychests.filter;

import static com.grahambartley.toomanychests.filter.TestCandidates.COBBLESTONE;
import static com.grahambartley.toomanychests.filter.TestCandidates.COGWHEEL;
import static com.grahambartley.toomanychests.filter.TestCandidates.COOKED_BEEF;
import static com.grahambartley.toomanychests.filter.TestCandidates.CREATE_WRENCH;
import static com.grahambartley.toomanychests.filter.TestCandidates.DARK_OAK_CHEST;
import static com.grahambartley.toomanychests.filter.TestCandidates.IRON_PICKAXE;
import static com.grahambartley.toomanychests.filter.TestCandidates.UNTAGGED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class FilterEvaluatorTest {
  private static final int LARGE_NETWORK_SIZE = 5_000;
  private static final int WARMUP_PASSES = 20;
  private static final int MEASURED_PASSES = 10;
  private static final long TICK_BUDGET_MILLIS = 50L;

  private static List<FilterCandidate> filter(ItemFilter filter) {
    return FilterEvaluator.filter(TestCandidates.all(), filter);
  }

  private static Stream<Arguments> tagCombinations() {
    return Stream.of(
        Arguments.of(TagCombinationMode.AND, Set.of("wood"), List.of(DARK_OAK_CHEST, COGWHEEL)),
        Arguments.of(TagCombinationMode.AND, Set.of("wood", "storage"), List.of(DARK_OAK_CHEST)),
        Arguments.of(TagCombinationMode.AND, Set.of("wood", "redstone"), List.of(COGWHEEL)),
        Arguments.of(TagCombinationMode.AND, Set.of("wood", "tools"), List.of()),
        Arguments.of(TagCombinationMode.OR, Set.of("wood"), List.of(DARK_OAK_CHEST, COGWHEEL)),
        Arguments.of(
            TagCombinationMode.OR,
            Set.of("wood", "tools"),
            List.of(DARK_OAK_CHEST, IRON_PICKAXE, COGWHEEL, CREATE_WRENCH)),
        Arguments.of(
            TagCombinationMode.OR, Set.of("storage", "food"), List.of(DARK_OAK_CHEST, COOKED_BEEF)),
        Arguments.of(TagCombinationMode.OR, Set.of("brewing"), List.of()));
  }

  @ParameterizedTest
  @MethodSource("tagCombinations")
  void tagFiltersCombineByMode(
      TagCombinationMode mode, Set<String> activeTags, List<FilterCandidate> expected) {
    assertEquals(expected, filter(ItemFilter.ofTags(mode, activeTags)));
  }

  private static Stream<Arguments> searches() {
    return Stream.of(
        Arguments.of("dch", List.of(DARK_OAK_CHEST)),
        Arguments.of("iron", List.of(IRON_PICKAXE)),
        Arguments.of("minecraft:cobblestone", List.of(COBBLESTONE)),
        Arguments.of("#tools", List.of(IRON_PICKAXE, CREATE_WRENCH)),
        Arguments.of("#wood", List.of(DARK_OAK_CHEST, COGWHEEL)),
        Arguments.of("@create", List.of(COGWHEEL, CREATE_WRENCH)),
        Arguments.of("@some_mod", List.of(UNTAGGED)),
        Arguments.of("@create cog", List.of(COGWHEEL)),
        Arguments.of("@create #tools", List.of(CREATE_WRENCH)),
        Arguments.of("#wood #redstone", List.of(COGWHEEL)),
        Arguments.of("@create @some_mod", List.of(COGWHEEL, CREATE_WRENCH, UNTAGGED)),
        Arguments.of("nothing_matches_this", List.of()));
  }

  @ParameterizedTest
  @MethodSource("searches")
  void eachSearchPrefixIsHonouredAloneAndWithFreeText(String raw, List<FilterCandidate> expected) {
    assertEquals(expected, filter(ItemFilter.ofSearch(raw)));
  }

  @Test
  void searchNarrowsWithinTheActiveTagsRatherThanSearchingPastThem() {
    ItemFilter filter =
        new ItemFilter(Set.of("tools"), TagCombinationMode.AND, SearchQueryParser.parse("@create"));

    assertEquals(List.of(CREATE_WRENCH), filter(filter));
  }

  @Test
  void aSearchThatMatchesOnlyOutsideTheActiveTagsReturnsNothing() {
    ItemFilter filter =
        new ItemFilter(Set.of("food"), TagCombinationMode.AND, SearchQueryParser.parse("@create"));

    assertTrue(filter(filter).isEmpty());
  }

  @Test
  void anEmptyFilterMatchesTheWholeNetwork() {
    assertEquals(TestCandidates.all(), filter(ItemFilter.NONE));
  }

  @ParameterizedTest
  @EnumSource(TagCombinationMode.class)
  void noActiveTagsMatchesTheWholeNetworkUnderEitherMode(TagCombinationMode mode) {
    assertEquals(TestCandidates.all(), filter(ItemFilter.ofTags(mode, Set.of())));
  }

  @Test
  void aFilterMatchingNothingReturnsEmptyRatherThanEverything() {
    assertTrue(filter(ItemFilter.ofTags(TagCombinationMode.AND, Set.of("brewing"))).isEmpty());
    assertTrue(filter(ItemFilter.ofSearch("#brewing")).isEmpty());
    assertTrue(filter(ItemFilter.ofSearch("@nonexistent_mod")).isEmpty());
  }

  @Test
  void anUntaggedItemIsExcludedByAnyActiveTag() {
    assertFalse(
        FilterEvaluator.accepts(
            UNTAGGED, ItemFilter.ofTags(TagCombinationMode.OR, Set.of("tools"))));
    assertTrue(FilterEvaluator.accepts(UNTAGGED, ItemFilter.NONE));
  }

  @Test
  void filteringPreservesTheOrderItWasGiven() {
    List<FilterCandidate> matches =
        filter(ItemFilter.ofTags(TagCombinationMode.OR, Set.of("wood", "tools")));

    assertEquals(List.of(DARK_OAK_CHEST, IRON_PICKAXE, COGWHEEL, CREATE_WRENCH), matches);
  }

  @Test
  void nullArgumentsAreRejectedRatherThanSilentlyMatching() {
    assertThrows(NullPointerException.class, () -> FilterEvaluator.accepts(null, ItemFilter.NONE));
    assertThrows(NullPointerException.class, () -> FilterEvaluator.accepts(COBBLESTONE, null));
    assertThrows(NullPointerException.class, () -> FilterEvaluator.filter(null, ItemFilter.NONE));
    assertThrows(
        NullPointerException.class, () -> FilterEvaluator.filter(TestCandidates.all(), null));
  }

  private static List<FilterCandidate> largeNetwork() {
    final List<FilterCandidate> candidates = new ArrayList<>(LARGE_NETWORK_SIZE);
    for (int i = 0; i < LARGE_NETWORK_SIZE; i++) {
      candidates.add(
          new FilterCandidate(
              "mod_" + (i % 20) + ":item_" + i,
              "Generated Item " + i,
              Set.of("tag_" + (i % 13), "group_" + (i % 7))));
    }
    return List.copyOf(candidates);
  }

  /**
   * The terminal filters on the server thread while the player types, so a pass over a large
   * network has to fit inside a tick with room to spare for everything else happening in it.
   */
  @Test
  void evaluatingAWholeNetworkFitsInsideATick() {
    final List<FilterCandidate> network = largeNetwork();
    final ItemFilter filter =
        new ItemFilter(
            Set.of("tag_3"), TagCombinationMode.AND, SearchQueryParser.parse("@mod_1 item #tag"));

    for (int warmup = 0; warmup < WARMUP_PASSES; warmup++) {
      FilterEvaluator.filter(network, filter);
    }

    long fastestNanos = Long.MAX_VALUE;
    for (int pass = 0; pass < MEASURED_PASSES; pass++) {
      final long startedAt = System.nanoTime();
      final List<FilterCandidate> matches = FilterEvaluator.filter(network, filter);
      fastestNanos = Math.min(fastestNanos, System.nanoTime() - startedAt);
      assertFalse(matches.isEmpty());
    }

    final long millis = TimeUnit.NANOSECONDS.toMillis(fastestNanos);
    assertTrue(
        millis < TICK_BUDGET_MILLIS,
        "filtering " + LARGE_NETWORK_SIZE + " entries took " + millis + "ms");
  }
}
