package com.grahambartley.totalstorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FuzzySearchAttachmentTest {
  @Test
  void fuzzySearchStartsOff() {
    assertFalse(FuzzySearchAttachment.DEFAULT);
  }

  @Test
  void noPlayerMeansNoFuzzySearch() {
    assertFalse(FuzzySearchAttachment.get(null));
  }

  static Stream<Arguments> filters() {
    return Stream.of(
        Arguments.of(ItemFilter.NONE.withFuzzy(true), ItemFilter.NONE),
        Arguments.of(ItemFilter.ofSearch("dpick").withFuzzy(true), ItemFilter.ofSearch("dpick")),
        Arguments.of(
            new ItemFilter(Set.of("ores"), TagCombinationMode.OR, SearchQuery.EMPTY, true),
            new ItemFilter(Set.of("ores"), TagCombinationMode.OR, SearchQuery.EMPTY)),
        Arguments.of(null, ItemFilter.NONE));
  }

  @ParameterizedTest
  @MethodSource("filters")
  void aFilterAppliedForNoPlayerKeepsItsTermsAndMatchesLiterally(
      ItemFilter filter, ItemFilter expected) {
    assertEquals(expected, FuzzySearchAttachment.applyTo(null, filter));
  }
}
