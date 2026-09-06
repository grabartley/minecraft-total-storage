package com.grahambartley.toomanychests.tag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkTagLookupTest {
  private static final NetworkTagSet TAGGED =
      NetworkTagSet.defaults()
          .withCustomTag("ores")
          .withCustomTag("smeltable")
          .withItem("ores", "minecraft:raw_iron")
          .withItem("smeltable", "minecraft:raw_iron")
          .withItem("ores", "minecraft:coal");

  @Test
  void anItemResolvesToEveryTagTheNetworkFilesItUnder() {
    NetworkTagLookup lookup = new NetworkTagLookup(TAGGED);

    assertEquals(Set.of("ores", "smeltable"), lookup.tagIdsFor("minecraft:raw_iron"));
    assertEquals(Set.of("ores"), lookup.tagIdsFor("minecraft:coal"));
  }

  @Test
  void anUntaggedItemResolvesToNothingRatherThanNull() {
    NetworkTagLookup lookup = new NetworkTagLookup(TAGGED);

    assertEquals(Set.of(), lookup.tagIdsFor("minecraft:cobblestone"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   "})
  void aBlankItemResolvesToNothing(String itemId) {
    assertEquals(Set.of(), new NetworkTagLookup(TAGGED).tagIdsFor(itemId));
  }

  @Test
  void aNullItemResolvesToNothing() {
    assertEquals(Set.of(), new NetworkTagLookup(TAGGED).tagIdsFor(null));
  }

  @Test
  void theSameQuestionAskedTwiceGivesTheSameAnswerWithoutRecomputing() {
    NetworkTagLookup lookup = new NetworkTagLookup(TAGGED);

    assertSame(lookup.tagIdsFor("minecraft:raw_iron"), lookup.tagIdsFor("minecraft:raw_iron"));
  }

  @Test
  void anItemIdIsFoldedSoCasingNeverSplitsTheCache() {
    NetworkTagLookup lookup = new NetworkTagLookup(TAGGED);

    assertSame(lookup.tagIdsFor("minecraft:raw_iron"), lookup.tagIdsFor("MINECRAFT:Raw_Iron"));
  }

  @Test
  void editingTheTagSetChangesTheAnswerRatherThanServingTheOldOne() {
    NetworkTagLookup lookup = new NetworkTagLookup(TAGGED);
    lookup.tagIdsFor("minecraft:raw_iron");

    lookup.update(TAGGED.withoutItem("smeltable", "minecraft:raw_iron"));

    assertEquals(Set.of("ores"), lookup.tagIdsFor("minecraft:raw_iron"));
  }

  @Test
  void removingATagAltogetherStopsItsItemsResolvingToIt() {
    NetworkTagLookup lookup = new NetworkTagLookup(TAGGED);
    lookup.tagIdsFor("minecraft:coal");

    lookup.update(TAGGED.withoutTag("ores"));

    assertEquals(Set.of(), lookup.tagIdsFor("minecraft:coal"));
  }

  @Test
  void anEditThatChangedNothingKeepsTheAnswersAlreadyComputed() {
    NetworkTagLookup lookup = new NetworkTagLookup(TAGGED);
    Set<String> before = lookup.tagIdsFor("minecraft:raw_iron");

    lookup.update(TAGGED.withItem("ores", "minecraft:raw_iron"));

    assertSame(before, lookup.tagIdsFor("minecraft:raw_iron"));
  }

  @Test
  void theLookupReportsTheSetItIsAnsweringFrom() {
    NetworkTagLookup lookup = new NetworkTagLookup(TAGGED);

    assertEquals(TAGGED, lookup.tags());

    NetworkTagSet updated = TAGGED.withCustomTag("crops");
    lookup.update(updated);

    assertEquals(updated, lookup.tags());
  }

  @Test
  void aLookupWithoutATagSetIsRefusedRatherThanFailingLater() {
    assertThrows(NullPointerException.class, () -> new NetworkTagLookup(null));
    assertThrows(NullPointerException.class, () -> new NetworkTagLookup(TAGGED).update(null));
  }
}
