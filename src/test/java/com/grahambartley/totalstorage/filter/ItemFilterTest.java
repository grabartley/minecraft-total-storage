package com.grahambartley.totalstorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ItemFilterTest {
  @Test
  void aFilterIsLiteralUnlessFuzzyMatchingIsAskedFor() {
    assertFalse(ItemFilter.NONE.fuzzy());
    assertFalse(ItemFilter.ofSearch("dpick").fuzzy());
    assertFalse(new ItemFilter(Set.of(), null, SearchQuery.EMPTY).fuzzy());
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void withFuzzyChangesOnlyHowNamesMatch(boolean fuzzy) {
    final ItemFilter filter =
        new ItemFilter(Set.of("ores"), TagCombinationMode.OR, SearchQueryParser.parse("iron"));

    final ItemFilter changed = filter.withFuzzy(fuzzy);

    assertEquals(fuzzy, changed.fuzzy());
    assertEquals(filter.activeTags(), changed.activeTags());
    assertEquals(filter.combinationMode(), changed.combinationMode());
    assertEquals(filter.query(), changed.query());
  }

  @Test
  void theEmptyFilterHasNoTagsAndNoQuery() {
    assertTrue(ItemFilter.NONE.isEmpty());
    assertTrue(ItemFilter.NONE.activeTags().isEmpty());
    assertSame(SearchQuery.EMPTY, ItemFilter.NONE.query());
    assertEquals(TagCombinationMode.AND, ItemFilter.NONE.combinationMode());
  }

  @Test
  void aFilterWithTagsOrAQueryIsNotEmpty() {
    assertFalse(ItemFilter.ofTags(TagCombinationMode.OR, Set.of("ores")).isEmpty());
    assertFalse(ItemFilter.ofSearch("gear").isEmpty());
  }

  @Test
  void aFilterOfOnlyBlankTagsIsEmpty() {
    assertTrue(ItemFilter.ofTags(TagCombinationMode.AND, Set.of("  ")).isEmpty());
  }

  @Test
  void aSearchOfOnlyWhitespaceIsEmpty() {
    assertTrue(ItemFilter.ofSearch("   ").isEmpty());
  }

  @Test
  void activeTagsAreNormalised() {
    ItemFilter filter = ItemFilter.ofTags(TagCombinationMode.AND, Set.of(" ORES ", "Tools"));

    assertEquals(Set.of("ores", "tools"), filter.activeTags());
  }

  @Test
  void aMissingCombinationModeFallsBackToTheDefault() {
    ItemFilter filter = new ItemFilter(Set.of("ores"), null, SearchQuery.EMPTY);

    assertEquals(TagCombinationMode.DEFAULT, filter.combinationMode());
  }

  @Test
  void aMissingQueryFallsBackToTheEmptyQuery() {
    ItemFilter filter = new ItemFilter(Set.of("ores"), TagCombinationMode.AND, null);

    assertSame(SearchQuery.EMPTY, filter.query());
  }

  @Test
  void ofSearchParsesPrefixes() {
    ItemFilter filter = ItemFilter.ofSearch("@create #tools gear");

    assertEquals(List.of("create"), filter.query().modIdTerms());
    assertEquals(List.of("tools"), filter.query().tagTerms());
    assertEquals(List.of("gear"), filter.query().nameTerms());
  }

  @Test
  void mutatingTheSourceTagsDoesNotChangeTheFilter() {
    Set<String> source = new HashSet<>(Set.of("ores"));
    ItemFilter filter = ItemFilter.ofTags(TagCombinationMode.AND, source);
    source.add("tools");

    assertEquals(Set.of("ores"), filter.activeTags());
  }
}
