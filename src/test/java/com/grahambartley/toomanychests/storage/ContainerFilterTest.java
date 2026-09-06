package com.grahambartley.toomanychests.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ContainerFilterTest {
  private static final String COAL = "minecraft:coal";
  private static final String IRON = "minecraft:raw_iron";

  @Test
  void aFilterNobodyConfiguredIsEmpty() {
    assertTrue(ContainerFilter.NONE.isEmpty());
    assertTrue(ContainerFilter.NONE.tagIds().isEmpty());
    assertTrue(ContainerFilter.NONE.itemIds().isEmpty());
  }

  @Test
  void anEmptyFilterAcceptsNothingRatherThanEverything() {
    assertFalse(ContainerFilter.NONE.accepts(COAL, Set.of("ores")));
  }

  @Test
  void anAcceptedItemIsTakenWhateverItsTags() {
    ContainerFilter filter = ContainerFilter.ofItems(COAL);

    assertTrue(filter.accepts(COAL, Set.of()));
    assertFalse(filter.accepts(IRON, Set.of()));
  }

  @Test
  void anItemIsTakenWhenOneOfItsTagsIsAccepted() {
    ContainerFilter filter = ContainerFilter.ofTags("ores", "wood");

    assertTrue(filter.accepts(IRON, Set.of("ores")));
    assertTrue(filter.accepts(IRON, Set.of("food", "wood")));
    assertFalse(filter.accepts(IRON, Set.of("food")));
  }

  @Test
  void anItemWithNoTagsAtAllIsStillTakenIfItWasNamedOutright() {
    ContainerFilter filter = new ContainerFilter(Set.of("ores"), Set.of(COAL));

    assertTrue(filter.accepts(COAL, null));
    assertFalse(filter.accepts(IRON, null));
  }

  @ParameterizedTest
  @CsvSource({"  Ores  , ores", "ORES, ores", "ores, ores"})
  void tagsAreNormalisedSoTheSameTagIsNeverStoredTwice(String given, String expected) {
    ContainerFilter filter = ContainerFilter.ofTags(given);

    assertEquals(Set.of(expected), filter.tagIds());
    assertTrue(filter.hasTag(expected));
    assertSame(filter, filter.withTag(given));
  }

  @ParameterizedTest
  @ValueSource(strings = {"", "   ", "\t"})
  void aBlankEntryIsNotAnEntry(String blank) {
    ContainerFilter filter = new ContainerFilter(Set.of(blank), Set.of(blank));

    assertTrue(filter.isEmpty());
  }

  @Test
  void nullsInsideACollectionAreDroppedRatherThanStored() {
    ContainerFilter filter =
        ContainerFilter.of(Arrays.asList("ores", null), Arrays.asList(null, COAL));

    assertEquals(Set.of("ores"), filter.tagIds());
    assertEquals(Set.of(COAL), filter.itemIds());
  }

  @Test
  void aNullCollectionIsReadAsNothingAccepted() {
    assertEquals(ContainerFilter.NONE, ContainerFilter.of(null, null));
    assertEquals(ContainerFilter.NONE, new ContainerFilter(null, null));
  }

  @Test
  void duplicatesCollapseRatherThanThrowing() {
    ContainerFilter filter = ContainerFilter.of(List.of("ores", "ORES"), List.of(COAL, COAL));

    assertEquals(Set.of("ores"), filter.tagIds());
    assertEquals(Set.of(COAL), filter.itemIds());
  }

  @Test
  void addingAndRemovingATagRoundTrips() {
    ContainerFilter filter = ContainerFilter.NONE.withTag("ores");

    assertTrue(filter.hasTag("ores"));
    assertEquals(ContainerFilter.NONE, filter.withoutTag("ores"));
  }

  @Test
  void addingAndRemovingAnItemRoundTrips() {
    ContainerFilter filter = ContainerFilter.NONE.withItem(COAL);

    assertTrue(filter.hasItem(COAL));
    assertEquals(ContainerFilter.NONE, filter.withoutItem(COAL));
  }

  @Test
  void removingSomethingThatWasNeverThereChangesNothing() {
    ContainerFilter filter = ContainerFilter.ofTags("ores");

    assertSame(filter, filter.withoutTag("wood"));
    assertSame(filter, filter.withoutItem(COAL));
    assertSame(filter, filter.withoutTag(null));
  }

  @Test
  void twoFiltersBuiltInEitherOrderCompareEqual() {
    ContainerFilter forwards = ContainerFilter.NONE.withTag("wood").withTag("ores").withItem(COAL);
    ContainerFilter backwards = ContainerFilter.NONE.withItem(COAL).withTag("ores").withTag("wood");

    assertEquals(forwards, backwards);
    assertEquals(forwards.hashCode(), backwards.hashCode());
    assertEquals(List.copyOf(forwards.tagIds()), List.copyOf(backwards.tagIds()));
  }

  @Test
  void aStoredFilterRefusesToBeChangedFromOutside() {
    ContainerFilter filter = ContainerFilter.ofTags("ores");

    assertThrows(UnsupportedOperationException.class, () -> filter.tagIds().add("wood"));
    assertThrows(UnsupportedOperationException.class, () -> filter.itemIds().add(COAL));
  }
}
