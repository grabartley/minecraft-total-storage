package com.grahambartley.toomanychests.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class NetworkIndexTest {
  private static final StorageOrigin FIRST = TestOrigins.at(0);
  private static final StorageOrigin SECOND = TestOrigins.at(1);

  private static NetworkIndex<String> index() {
    return new NetworkIndexBuilder<String>()
        .add("cobblestone", 300L, FIRST)
        .add("cobblestone", 64L, SECOND)
        .add("diamond", 12L, SECOND)
        .add("torch", 5L, FIRST)
        .build(NetworkIndexOrdering.byKey(Function.identity()));
  }

  @ParameterizedTest
  @CsvSource({"cobblestone,364", "diamond,12", "torch,5"})
  void countOfReturnsTheAggregatedTotal(String variant, long expected) {
    assertEquals(expected, index().countOf(variant));
  }

  @Test
  void countOfAnAbsentVariantIsZeroRatherThanAFailure() {
    assertEquals(0L, index().countOf("netherite_ingot"));
  }

  @Test
  void countOfNullIsZero() {
    assertEquals(0L, index().countOf(null));
  }

  @Test
  void entryCarriesEveryContainerHoldingTheVariant() {
    NetworkIndexEntry<String> entry = index().entry("cobblestone").orElseThrow();

    assertEquals(List.of(FIRST, SECOND), entry.origins());
  }

  @Test
  void entryOfAnAbsentVariantIsEmpty() {
    assertTrue(index().entry("netherite_ingot").isEmpty());
  }

  @Test
  void entryOfNullIsEmpty() {
    assertTrue(index().entry(null).isEmpty());
  }

  @Test
  void containsDistinguishesHeldFromAbsentVariants() {
    NetworkIndex<String> index = index();

    assertTrue(index.contains("diamond"));
    assertFalse(index.contains("netherite_ingot"));
    assertFalse(index.contains(null));
  }

  @Test
  void variantCountIsTheNumberOfGridCells() {
    assertEquals(3, index().variantCount());
  }

  @Test
  void totalCountSumsEveryEntry() {
    assertEquals(381L, index().totalCount());
  }

  @Test
  void totalCountSaturatesRatherThanWrappingNegative() {
    NetworkIndex<String> index =
        new NetworkIndexBuilder<String>()
            .add("stone", Long.MAX_VALUE, FIRST)
            .add("dirt", 64L, SECOND)
            .build(NetworkIndexOrdering.byKey(Function.identity()));

    assertEquals(Long.MAX_VALUE, index.totalCount());
  }

  @Test
  void entriesAreUnmodifiable() {
    NetworkIndex<String> index = index();
    NetworkIndexEntry<String> extra = new NetworkIndexEntry<>("gravel", 1L, List.of(FIRST));

    assertThrows(UnsupportedOperationException.class, () -> index.entries().add(extra));
  }

  @Test
  void anEmptyIndexAnswersEveryQuerySafely() {
    NetworkIndex<String> empty = NetworkIndex.empty();

    assertTrue(empty.isEmpty());
    assertEquals(0, empty.variantCount());
    assertEquals(0L, empty.totalCount());
    assertEquals(List.of(), empty.entries());
  }

  @Test
  void anIndexBuiltFromNothingIsTheSharedEmptyIndex() {
    NetworkIndex<String> built =
        new NetworkIndexBuilder<String>().build(NetworkIndexOrdering.byKey(Function.identity()));

    assertSame(NetworkIndex.empty(), built);
  }
}
