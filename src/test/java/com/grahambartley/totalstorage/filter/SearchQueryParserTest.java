package com.grahambartley.totalstorage.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class SearchQueryParserTest {
  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t\n"})
  void nothingTypedParsesToTheEmptyQuery(String raw) {
    assertSame(SearchQuery.EMPTY, SearchQueryParser.parse(raw));
  }

  @Test
  void bareTextBecomesNameTerms() {
    SearchQuery query = SearchQueryParser.parse("dark oak");

    assertEquals(List.of("dark", "oak"), query.nameTerms());
    assertTrue(query.tagTerms().isEmpty());
    assertTrue(query.modIdTerms().isEmpty());
  }

  @Test
  void hashPrefixedTermsBecomeTagTerms() {
    assertEquals(List.of("ores"), SearchQueryParser.parse("#ores").tagTerms());
  }

  @Test
  void atPrefixedTermsBecomeModIdTerms() {
    assertEquals(List.of("create"), SearchQueryParser.parse("@create").modIdTerms());
  }

  @Test
  void prefixesCombineWithBareText() {
    SearchQuery query = SearchQueryParser.parse("@create gear #tools");

    assertEquals(List.of("gear"), query.nameTerms());
    assertEquals(List.of("tools"), query.tagTerms());
    assertEquals(List.of("create"), query.modIdTerms());
  }

  @Test
  void termsAreNormalisedAsTheyAreParsed() {
    SearchQuery query = SearchQueryParser.parse("  @Create   Gear   #Tools  ");

    assertEquals(List.of("gear"), query.nameTerms());
    assertEquals(List.of("tools"), query.tagTerms());
    assertEquals(List.of("create"), query.modIdTerms());
  }

  @ParameterizedTest
  @ValueSource(strings = {"#", "@", "# @"})
  void aPrefixWithNothingAfterItIsDroppedRatherThanMatchingEverything(String raw) {
    assertTrue(SearchQueryParser.parse(raw).isEmpty());
  }

  @Test
  void aPrefixCharacterInsideATermIsPlainText() {
    SearchQuery query = SearchQueryParser.parse("minecraft:iron_ore");

    assertEquals(List.of("minecraft:iron_ore"), query.nameTerms());
    assertTrue(query.tagTerms().isEmpty());
  }

  @Test
  void repeatedPrefixesAreKeptSeparatelySoEachIsHonoured() {
    SearchQuery query = SearchQueryParser.parse("#ores #tools @create @minecraft");

    assertEquals(List.of("ores", "tools"), query.tagTerms());
    assertEquals(List.of("create", "minecraft"), query.modIdTerms());
  }
}
