package com.grahambartley.totalstorage.filter;

import static com.grahambartley.totalstorage.filter.TestCandidates.COBBLESTONE;
import static com.grahambartley.totalstorage.filter.TestCandidates.COGWHEEL;
import static com.grahambartley.totalstorage.filter.TestCandidates.COOKED_BEEF;
import static com.grahambartley.totalstorage.filter.TestCandidates.CREATE_WRENCH;
import static com.grahambartley.totalstorage.filter.TestCandidates.DARK_OAK_CHEST;
import static com.grahambartley.totalstorage.filter.TestCandidates.IRON_PICKAXE;
import static com.grahambartley.totalstorage.filter.TestCandidates.UNTAGGED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class FilterEvaluatorTest {
  private static List<FilterCandidate> filter(ItemFilter filter) {
    return TestCandidates.all().stream()
        .filter(candidate -> FilterEvaluator.strengthOf(candidate, filter).isMatch())
        .toList();
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
        Arguments.of("dch", List.of()),
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

  private static Stream<Arguments> fuzzySearches() {
    return Stream.of(
        Arguments.of("dch", true, List.of(DARK_OAK_CHEST)),
        Arguments.of("dch", false, List.of()),
        Arguments.of("@create cgw", true, List.of(COGWHEEL)),
        Arguments.of("@create cgw", false, List.of()),
        Arguments.of("#stn", true, List.of()),
        Arguments.of("chest", false, List.of(DARK_OAK_CHEST)));
  }

  @ParameterizedTest
  @MethodSource("fuzzySearches")
  void nameTermsMatchLettersInOrderOnlyWhenTheFilterIsFuzzy(
      String raw, boolean fuzzy, List<FilterCandidate> expected) {
    assertEquals(expected, filter(ItemFilter.ofSearch(raw).withFuzzy(fuzzy)));
  }

  private static Stream<Arguments> identifierTerms() {
    return Stream.of(
        Arguments.of("#stone", List.of(COBBLESTONE)),
        Arguments.of("#red", List.of(COGWHEEL)),
        Arguments.of("#tone", List.of()),
        Arguments.of("#blocks", List.of(COBBLESTONE)),
        Arguments.of("#drops", List.of(COOKED_BEEF)),
        Arguments.of("#_drops", List.of()),
        Arguments.of("#stne", List.of()),
        Arguments.of("@cre", List.of(COGWHEEL, CREATE_WRENCH)),
        Arguments.of("@eate", List.of()),
        Arguments.of("@crt", List.of()),
        Arguments.of("@some", List.of(UNTAGGED)),
        Arguments.of("@mod", List.of(UNTAGGED)),
        Arguments.of("@od", List.of()));
  }

  @ParameterizedTest
  @MethodSource("identifierTerms")
  void tagAndModTermsMatchAnIdentifierByExactOrPrefixOnly(
      String raw, List<FilterCandidate> expected) {
    assertEquals(expected, filter(ItemFilter.ofSearch(raw)));
  }

  @ParameterizedTest
  @MethodSource("baseKitTerms")
  void aMultiWordTagIsFoundByItsWholeIdOrAnyWordsPrefix(String raw, boolean expected) {
    FilterCandidate kit =
        new FilterCandidate("minecraft:torch", "Torch", Set.of(FilterText.tagId("Base Kit")));

    assertEquals(expected, accepts(kit, ItemFilter.ofSearch(raw)));
  }

  private static Stream<Arguments> baseKitTerms() {
    return Stream.of(
        Arguments.of("#base-kit", true),
        Arguments.of("#base", true),
        Arguments.of("#kit", true),
        Arguments.of("#it", false),
        Arguments.of("#bkit", false));
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
    assertFalse(accepts(UNTAGGED, ItemFilter.ofTags(TagCombinationMode.OR, Set.of("tools"))));
    assertTrue(accepts(UNTAGGED, ItemFilter.NONE));
  }

  @Test
  void nullArgumentsAreRejectedRatherThanSilentlyMatching() {
    assertThrows(
        NullPointerException.class, () -> FilterEvaluator.strengthOf(null, ItemFilter.NONE));
    assertThrows(NullPointerException.class, () -> FilterEvaluator.strengthOf(COBBLESTONE, null));
  }

  private static boolean accepts(FilterCandidate candidate, ItemFilter filter) {
    return FilterEvaluator.strengthOf(candidate, filter).isMatch();
  }
}
