package com.grahambartley.totalstorage.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkIndexBuilderTest {
  private static final Comparator<NetworkIndexEntry<String>> BY_NAME =
      NetworkIndexOrdering.byKey(Function.identity());
  private static final StorageOrigin FIRST = TestOrigins.at(0);
  private static final StorageOrigin SECOND = TestOrigins.at(1);
  private static final StorageOrigin THIRD = TestOrigins.at(2);

  private static final long TICK_NANOS = 50_000_000L;

  @Test
  void identicalVariantsInDifferentContainersAggregateIntoOneEntry() {
    NetworkIndex<String> index =
        new NetworkIndexBuilder<String>()
            .add("cobblestone", 64L, FIRST)
            .add("cobblestone", 128L, SECOND)
            .add("cobblestone", 108L, THIRD)
            .build(BY_NAME);

    assertEquals(1, index.variantCount());
    assertEquals(300L, index.countOf("cobblestone"));
  }

  @Test
  void repeatedSlotsInOneContainerAggregateWithoutRepeatingTheOrigin() {
    NetworkIndex<String> index =
        new NetworkIndexBuilder<String>()
            .add("cobblestone", 64L, FIRST)
            .add("cobblestone", 64L, FIRST)
            .build(BY_NAME);

    NetworkIndexEntry<String> entry = index.entry("cobblestone").orElseThrow();
    assertEquals(128L, entry.totalCount());
    assertEquals(List.of(FIRST), entry.origins());
  }

  @Test
  void variantsThatDifferOnlyByComponentsStaySeparateEntries() {
    NetworkIndex<String> index =
        new NetworkIndexBuilder<String>()
            .add("diamond_sword", 1L, FIRST)
            .add("diamond_sword{sharpness=5}", 1L, FIRST)
            .build(BY_NAME);

    assertEquals(2, index.variantCount());
    assertEquals(1L, index.countOf("diamond_sword"));
    assertEquals(1L, index.countOf("diamond_sword{sharpness=5}"));
  }

  @Test
  void originsAreRecordedInTheOrderTheyWereFirstSeen() {
    NetworkIndex<String> index =
        new NetworkIndexBuilder<String>()
            .add("stone", 1L, SECOND)
            .add("stone", 1L, FIRST)
            .add("stone", 1L, SECOND)
            .build(BY_NAME);

    assertEquals(List.of(SECOND, FIRST), index.entry("stone").orElseThrow().origins());
  }

  @Test
  void anEmptyNetworkProducesAnEmptyIndex() {
    NetworkIndex<String> index = new NetworkIndexBuilder<String>().build(BY_NAME);

    assertTrue(index.isEmpty());
    assertEquals(0L, index.countOf("stone"));
  }

  @Test
  void aZeroAmountDoesNotCreateAnEntry() {
    NetworkIndex<String> index =
        new NetworkIndexBuilder<String>().add("stone", 0L, FIRST).build(BY_NAME);

    assertTrue(index.isEmpty());
  }

  @Test
  void aZeroAmountDoesNotAddAnOriginToAnExistingEntry() {
    NetworkIndex<String> index =
        new NetworkIndexBuilder<String>()
            .add("stone", 5L, FIRST)
            .add("stone", 0L, SECOND)
            .build(BY_NAME);

    assertEquals(List.of(FIRST), index.entry("stone").orElseThrow().origins());
  }

  @Test
  void countsSaturateRatherThanWrappingNegative() {
    NetworkIndex<String> index =
        new NetworkIndexBuilder<String>()
            .add("stone", Long.MAX_VALUE, FIRST)
            .add("stone", 64L, SECOND)
            .build(BY_NAME);

    assertEquals(Long.MAX_VALUE, index.countOf("stone"));
  }

  @ParameterizedTest
  @ValueSource(longs = {-1L, Long.MIN_VALUE})
  void aNegativeAmountIsRejected(long amount) {
    NetworkIndexBuilder<String> builder = new NetworkIndexBuilder<>();

    assertThrows(IllegalArgumentException.class, () -> builder.add("stone", amount, FIRST));
  }

  @Test
  void nullVariantIsRejected() {
    NetworkIndexBuilder<String> builder = new NetworkIndexBuilder<>();

    assertThrows(NullPointerException.class, () -> builder.add(null, 1L, FIRST));
  }

  @Test
  void nullOriginIsRejected() {
    NetworkIndexBuilder<String> builder = new NetworkIndexBuilder<>();

    assertThrows(NullPointerException.class, () -> builder.add("stone", 1L, null));
  }

  @Test
  void nullOrderingIsRejected() {
    NetworkIndexBuilder<String> builder = new NetworkIndexBuilder<>();

    assertThrows(NullPointerException.class, () -> builder.build(null));
  }

  @Test
  void aFullNetworkOfFiftyDoubleChestsIndexesWellWithinOneTick() {
    buildLargeNetwork();

    long startedAt = System.nanoTime();
    NetworkIndex<String> index = buildLargeNetwork();
    long elapsedNanos = System.nanoTime() - startedAt;

    assertEquals(2700, index.variantCount());
    assertTrue(
        elapsedNanos < TICK_NANOS,
        "indexing 50 double chests took " + elapsedNanos / 1_000_000L + " ms");
  }

  private static NetworkIndex<String> buildLargeNetwork() {
    NetworkIndexBuilder<String> builder = new NetworkIndexBuilder<>();
    for (int chest = 0; chest < 50; chest++) {
      StorageOrigin origin = TestOrigins.at(chest);
      for (int slot = 0; slot < 54; slot++) {
        builder.add("item_" + (chest * 54 + slot), 64L, origin);
      }
    }
    return builder.build(BY_NAME);
  }
}
