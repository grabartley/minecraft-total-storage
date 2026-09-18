package com.grahambartley.toomanychests.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FilterTextTest {
  @ParameterizedTest
  @CsvSource({
    "Cobblestone,cobblestone",
    "  Dark Oak Chest  ,dark oak chest",
    "MINECRAFT:STONE,minecraft:stone",
    "already,already"
  })
  void normaliseLowercasesAndTrims(String raw, String expected) {
    assertEquals(expected, FilterText.normalise(raw));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"   ", "\t"})
  void normaliseCollapsesNothingToTheEmptyString(String raw) {
    assertEquals("", FilterText.normalise(raw));
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"  "})
  void isBlankIsTrueForNothing(String raw) {
    assertTrue(FilterText.isBlank(raw));
  }

  @Test
  void isBlankIsFalseForRealText() {
    assertFalse(FilterText.isBlank(" a "));
  }

  @Test
  void normaliseAllNormalisesEveryEntry() {
    assertEquals(Set.of("ores", "tools"), FilterText.normaliseAll(List.of("Ores", " TOOLS ")));
  }

  @Test
  void normaliseAllDropsBlanksRatherThanKeepingEmptyTerms() {
    assertEquals(Set.of("ores"), FilterText.normaliseAll(Arrays.asList("Ores", "  ", null, "")));
  }

  @Test
  void normaliseAllCollapsesEntriesThatDifferOnlyByCase() {
    assertEquals(Set.of("ores"), FilterText.normaliseAll(List.of("Ores", "ORES", "ores")));
  }

  @Test
  void normaliseAllOfNothingIsEmpty() {
    assertTrue(FilterText.normaliseAll(null).isEmpty());
    assertTrue(FilterText.normaliseAll(List.of()).isEmpty());
  }

  @Test
  void normaliseAllPreservesEncounterOrder() {
    assertEquals(
        List.of("wood", "ores", "tools"),
        List.copyOf(FilterText.normaliseAll(List.of("Wood", "Ores", "Tools"))));
  }

  @Test
  void normaliseAllResultRejectsMutation() {
    Set<String> normalised = FilterText.normaliseAll(List.of("ores"));

    assertThrows(UnsupportedOperationException.class, () -> normalised.add("tools"));
  }

  @ParameterizedTest
  @CsvSource({
    "minecraft:cobblestone,true",
    "create:,true",
    ":cobblestone,true",
    "cobblestone,false",
    "'',false"
  })
  void aTermNamesANamespaceOnlyWhenItCarriesTheSeparator(String value, boolean expected) {
    assertEquals(expected, FilterText.hasNamespace(value));
  }

  @Test
  void nothingNamesNoNamespace() {
    assertFalse(FilterText.hasNamespace(null));
  }

  @ParameterizedTest
  @CsvSource({
    "Castle,castle",
    "'  Base Build  ',base-build",
    "REDSTONE-project,redstone-project",
    "'Base   Kit',base-kit",
    "'a b c',a-b-c"
  })
  void aTagIdIsItsNameNormalisedIntoOneSearchableToken(String name, String expected) {
    assertEquals(expected, FilterText.tagId(name));
  }

  @ParameterizedTest
  @ValueSource(strings = {"a b", "a\tb", "a\nb", "a\u000bb", "a\fb", "a\rb", "a  b"})
  void everyKindOfWhitespaceBecomesTheSameSeparator(String name) {
    assertEquals("a-b", FilterText.tagId(name));
  }

  @Test
  void aTagIdWithNoWhitespaceIsHandedBackAsTheNormalisedStringItAlreadyWas() {
    assertEquals("base-kit", FilterText.tagId("Base-Kit"));
    assertEquals("minecraft:stone", FilterText.tagId("minecraft:stone"));
    assertEquals("", FilterText.tagId(null));
  }

  @Test
  void tagIdsDropWhatCanonicalisesToNothingAndKeepTheirOrder() {
    assertEquals(
        List.of("base-kit", "castle"),
        List.copyOf(FilterText.tagIds(Arrays.asList("Base Kit", "  ", null, "Castle"))));
    assertEquals(Set.of(), FilterText.tagIds(null));
  }

  @Test
  void anItemIdKeepsItsSpacingRuleSeparateFromATagId() {
    assertEquals("a b", FilterText.normalise("A B"));
    assertEquals("a-b", FilterText.tagId("A B"));
  }
}
