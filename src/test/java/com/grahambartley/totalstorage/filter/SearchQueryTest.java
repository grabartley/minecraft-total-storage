package com.grahambartley.totalstorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class SearchQueryTest {
  @Test
  void theEmptyQueryHasNoTermsOfAnyKind() {
    assertTrue(SearchQuery.EMPTY.isEmpty());
    assertTrue(SearchQuery.EMPTY.nameTerms().isEmpty());
    assertTrue(SearchQuery.EMPTY.tagTerms().isEmpty());
    assertTrue(SearchQuery.EMPTY.modIdTerms().isEmpty());
  }

  @Test
  void aQueryWithAnyTermIsNotEmpty() {
    assertFalse(new SearchQuery(List.of("gear"), List.of(), List.of()).isEmpty());
    assertFalse(new SearchQuery(List.of(), List.of("ores"), List.of()).isEmpty());
    assertFalse(new SearchQuery(List.of(), List.of(), List.of("create")).isEmpty());
  }

  @Test
  void termsAreNormalisedOnConstruction() {
    SearchQuery query = new SearchQuery(List.of(" Gear "), List.of("ORES"), List.of(" Create"));

    assertEquals(List.of("gear"), query.nameTerms());
    assertEquals(List.of("ores"), query.tagTerms());
    assertEquals(List.of("create"), query.modIdTerms());
  }

  @Test
  void blankAndNullTermsAreDroppedRatherThanMatchingEverything() {
    SearchQuery query = new SearchQuery(Arrays.asList("gear", "  ", null, ""), null, null);

    assertEquals(List.of("gear"), query.nameTerms());
    assertTrue(query.tagTerms().isEmpty());
    assertTrue(query.modIdTerms().isEmpty());
  }

  @Test
  void aQueryOfOnlyBlanksIsEmpty() {
    assertTrue(new SearchQuery(List.of("  "), List.of(""), List.of(" ")).isEmpty());
  }

  @Test
  void termListsRejectMutation() {
    SearchQuery query = new SearchQuery(List.of("gear"), List.of(), List.of());

    assertThrows(UnsupportedOperationException.class, () -> query.nameTerms().add("extra"));
  }

  @Test
  void mutatingTheSourceListDoesNotChangeTheQuery() {
    List<String> source = new ArrayList<>(List.of("gear"));
    SearchQuery query = new SearchQuery(source, List.of(), List.of());
    source.add("extra");

    assertEquals(List.of("gear"), query.nameTerms());
  }
}
