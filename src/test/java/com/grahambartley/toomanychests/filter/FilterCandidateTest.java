package com.grahambartley.toomanychests.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class FilterCandidateTest {
  private static FilterCandidate candidate(String identifier) {
    return new FilterCandidate(identifier, "Cobblestone", Set.of("stone"));
  }

  @ParameterizedTest
  @CsvSource({
    "minecraft:cobblestone,minecraft",
    "create:cogwheel,create",
    "some_mod:thing:with:colons,some_mod"
  })
  void modIdIsTheNamespace(String identifier, String expected) {
    assertEquals(expected, candidate(identifier).modId());
  }

  @ParameterizedTest
  @CsvSource({
    "minecraft:cobblestone,cobblestone",
    "create:cogwheel,cogwheel",
    "some_mod:thing:with:colons,thing:with:colons"
  })
  void pathIsEverythingAfterTheNamespace(String identifier, String expected) {
    assertEquals(expected, candidate(identifier).path());
  }

  @Test
  void anIdentifierWithoutANamespaceIsTreatedAsVanilla() {
    FilterCandidate bare = candidate("cobblestone");

    assertEquals("minecraft", bare.modId());
    assertEquals("cobblestone", bare.path());
  }

  @Test
  void identifierDisplayNameAndTagsAreAllNormalised() {
    FilterCandidate normalised =
        new FilterCandidate(" MINECRAFT:Cobblestone ", " Cobblestone ", Set.of("Stone", " TOOLS "));

    assertEquals("minecraft:cobblestone", normalised.identifier());
    assertEquals("cobblestone", normalised.displayName());
    assertEquals(Set.of("stone", "tools"), normalised.tags());
  }

  @Test
  void aMissingDisplayNameIsEmptyRatherThanNull() {
    assertEquals("", new FilterCandidate("minecraft:stone", null, Set.of()).displayName());
  }

  @ParameterizedTest
  @EmptySource
  @ValueSource(strings = {"   "})
  void aBlankIdentifierIsRejected(String identifier) {
    assertThrows(
        IllegalArgumentException.class, () -> new FilterCandidate(identifier, "Stone", Set.of()));
  }

  @Test
  void aNullIdentifierIsRejected() {
    assertThrows(NullPointerException.class, () -> new FilterCandidate(null, "Stone", Set.of()));
  }

  @Test
  void hasTagIgnoresCasing() {
    FilterCandidate stone = candidate("minecraft:cobblestone");

    assertTrue(stone.hasTag("STONE"));
    assertTrue(stone.hasTag(" stone "));
    assertFalse(stone.hasTag("tools"));
    assertFalse(stone.hasTag(null));
  }

  @Test
  void mutatingTheSourceTagsDoesNotChangeTheCandidate() {
    Set<String> source = new HashSet<>(Set.of("stone"));
    FilterCandidate stone = new FilterCandidate("minecraft:cobblestone", "Cobblestone", source);
    source.add("tools");

    assertEquals(Set.of("stone"), stone.tags());
  }

  @Test
  void aNetworksOwnTagsJoinTheOnesTheItemAlreadyCarried() {
    FilterCandidate candidate =
        new FilterCandidate("minecraft:oak_log", "Oak Log", Set.of("wood"))
            .plusTags(Set.of("castle", "keep"));

    assertEquals(Set.of("wood", "castle", "keep"), candidate.tags());
  }

  @Test
  void anExtraTagTheItemAlreadyCarriedDoesNotDuplicateIt() {
    FilterCandidate original = new FilterCandidate("minecraft:oak_log", "Oak Log", Set.of("wood"));

    assertSame(original, original.plusTags(Set.of("WOOD")));
    assertSame(original, original.plusTags(Set.of()));
    assertSame(original, original.plusTags(null));
  }

  @Test
  void addingTagsLeavesTheItemItselfAlone() {
    FilterCandidate candidate =
        new FilterCandidate("minecraft:oak_log", "Oak Log", Set.of("wood"))
            .plusTags(Set.of("castle"));

    assertEquals("minecraft:oak_log", candidate.identifier());
    assertEquals("oak log", candidate.displayName());
  }
}
