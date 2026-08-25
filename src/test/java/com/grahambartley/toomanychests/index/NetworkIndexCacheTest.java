package com.grahambartley.toomanychests.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkIndexCacheTest {
  private static final Comparator<NetworkIndexEntry<String>> BY_NAME =
      NetworkIndexOrdering.byKey(Function.identity());
  private static final StorageOrigin FIRST = TestOrigins.at(0);
  private static final StorageOrigin SECOND = TestOrigins.at(1);
  private static final StorageOrigin THIRD = TestOrigins.at(2);
  private static final StorageOrigin OUTSIDE = TestOrigins.at(99);

  private final FakeContainers containers = new FakeContainers();
  private final NetworkIndexCache<String> cache = new NetworkIndexCache<>(containers, BY_NAME);

  @Test
  void anUntrackedCacheReadsNothingAndIndexesNothing() {
    assertTrue(cache.index().isEmpty());
    assertEquals(0, containers.totalReads());
    assertEquals(0, cache.recomputeCount());
  }

  @Test
  void trackingContainersDoesNotReadThemYet() {
    containers.put(FIRST, "cobblestone", 64L);
    cache.track(List.of(FIRST, SECOND));

    assertEquals(0, containers.totalReads());
    assertEquals(0, cache.recomputeCount());
    assertTrue(cache.isStale());
  }

  @Test
  void theFirstReadBuildsTheIndexFromEveryTrackedContainer() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "cobblestone", 32L);
    cache.track(List.of(FIRST, SECOND));

    assertEquals(96L, cache.index().countOf("cobblestone"));
    assertEquals(1, cache.recomputeCount());
    assertEquals(2, cache.containerReadCount());
  }

  @Test
  void anIdleNetworkNeverRecomputesNoMatterHowOftenItIsRead() {
    containers.put(FIRST, "cobblestone", 64L);
    cache.track(List.of(FIRST));
    cache.index();

    for (int read = 0; read < 200; read++) {
      cache.index();
    }

    assertEquals(1, cache.recomputeCount());
    assertEquals(1, cache.containerReadCount());
  }

  @Test
  void aMutationIsNotActedOnUntilSomethingReadsTheIndex() {
    containers.put(FIRST, "cobblestone", 64L);
    cache.track(List.of(FIRST));
    cache.index();

    containers.put(FIRST, "cobblestone", 128L);
    cache.invalidate(FIRST);

    assertEquals(1, cache.recomputeCount());
    assertTrue(cache.isStale());
    assertEquals(128L, cache.index().countOf("cobblestone"));
    assertEquals(2, cache.recomputeCount());
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 8, 64})
  void aBurstOfMutationsBetweenTwoReadsCostsOneReRead(int mutations) {
    containers.put(FIRST, "cobblestone", 1L);
    cache.track(List.of(FIRST));
    cache.index();

    for (int mutation = 1; mutation <= mutations; mutation++) {
      containers.put(FIRST, "cobblestone", (long) mutation);
      cache.invalidate(FIRST);
    }

    assertEquals((long) mutations, cache.index().countOf("cobblestone"));
    assertEquals(2, cache.recomputeCount());
    assertEquals(2, containers.readsOf(FIRST));
  }

  @Test
  void recomputingReReadsOnlyTheContainerThatChanged() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    containers.put(THIRD, "torch", 5L);
    cache.track(List.of(FIRST, SECOND, THIRD));
    cache.index();

    containers.put(SECOND, "diamond", 9L);
    cache.invalidate(SECOND);
    cache.index();

    assertEquals(1, containers.readsOf(FIRST));
    assertEquals(2, containers.readsOf(SECOND));
    assertEquals(1, containers.readsOf(THIRD));
  }

  @Test
  void untouchedContainersKeepContributingAfterOneOfThemChanges() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "cobblestone", 32L);
    cache.track(List.of(FIRST, SECOND));
    cache.index();

    containers.put(FIRST, "cobblestone", 1L);
    cache.invalidate(FIRST);

    NetworkIndexEntry<String> entry = cache.index().entry("cobblestone").orElseThrow();
    assertEquals(33L, entry.totalCount());
    assertEquals(List.of(FIRST, SECOND), entry.origins());
  }

  @Test
  void emptyingAContainerRemovesItsRowWithoutTouchingTheOthers() {
    containers.put(FIRST, "diamond", 8L);
    containers.put(SECOND, "cobblestone", 64L);
    cache.track(List.of(FIRST, SECOND));
    cache.index();

    containers.clear(FIRST);
    cache.invalidate(FIRST);

    assertFalse(cache.index().contains("diamond"));
    assertEquals(64L, cache.index().countOf("cobblestone"));
  }

  @Test
  void aMutationInAContainerOutsideTheNetworkIsIgnored() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(OUTSIDE, "diamond", 8L);
    cache.track(List.of(FIRST));
    cache.index();

    cache.invalidate(OUTSIDE);

    assertFalse(cache.isStale());
    assertEquals(1, cache.recomputeCount());
    assertEquals(0, containers.readsOf(OUTSIDE));
  }

  @Test
  void aMutationBeforeTheFirstReadStillCostsOnlyOneRecompute() {
    containers.put(FIRST, "cobblestone", 64L);
    cache.track(List.of(FIRST));

    cache.invalidate(FIRST);
    cache.index();

    assertEquals(1, cache.recomputeCount());
    assertEquals(1, containers.readsOf(FIRST));
  }

  @Test
  void reTrackingReplacesTheNetworkRatherThanAddingToIt() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    cache.track(List.of(FIRST));
    cache.index();

    cache.track(List.of(SECOND));

    assertEquals(1, cache.trackedContainerCount());
    assertEquals(0L, cache.index().countOf("cobblestone"));
    assertEquals(8L, cache.index().countOf("diamond"));
  }

  @Test
  void trackingNothingEmptiesTheIndexWithoutReadingAnything() {
    containers.put(FIRST, "cobblestone", 64L);
    cache.track(List.of(FIRST));
    cache.index();
    int readsBefore = containers.totalReads();

    cache.track(List.of());

    assertTrue(cache.index().isEmpty());
    assertEquals(readsBefore, containers.totalReads());
  }

  @Test
  void indexOrderFollowsDiscoveryOrderForEntriesTheComparatorCannotSeparate() {
    containers.put(SECOND, "cobblestone", 10L);
    containers.put(FIRST, "cobblestone", 10L);
    cache.track(List.of(SECOND, FIRST));

    NetworkIndexEntry<String> entry = cache.index().entry("cobblestone").orElseThrow();

    assertEquals(List.of(SECOND, FIRST), entry.origins());
  }

  @Test
  void aContainerReadingAsEmptyContributesNoRow() {
    cache.track(List.of(FIRST));

    assertTrue(cache.index().isEmpty());
    assertEquals(1, cache.recomputeCount());
  }

  @Test
  void constructionRejectsMissingCollaborators() {
    assertThrows(NullPointerException.class, () -> new NetworkIndexCache<String>(null, BY_NAME));
    assertThrows(NullPointerException.class, () -> new NetworkIndexCache<>(containers, null));
  }

  @Test
  void trackingRejectsMissingOrigins() {
    assertThrows(NullPointerException.class, () -> cache.track(null));
  }

  @Test
  void adoptingAContainerAddsWhatItHoldsOnTheNextRead() {
    containers.put(FIRST, "cobblestone", 16L);
    containers.put(SECOND, "diamond", 4L);
    cache.track(List.of(FIRST));
    cache.index();

    cache.adopt(SECOND);

    assertEquals(4L, cache.index().countOf("diamond"));
    assertEquals(2, cache.trackedContainerCount());
  }

  @Test
  void adoptingAContainerAlreadyTrackedNeitherReReadsItNorDuplicatesIt() {
    containers.put(FIRST, "cobblestone", 16L);
    cache.track(List.of(FIRST));
    cache.index();
    int readsBefore = containers.totalReads();

    cache.adopt(FIRST);

    assertFalse(cache.isStale());
    assertEquals(readsBefore, containers.totalReads());
    assertEquals(1, cache.trackedContainerCount());
    assertEquals(16L, cache.index().countOf("cobblestone"));
  }

  @Test
  void forgettingAContainerTakesEverythingItContributedWithIt() {
    containers.put(FIRST, "cobblestone", 16L);
    containers.put(SECOND, "cobblestone", 32L);
    cache.track(List.of(FIRST, SECOND));
    cache.index();

    cache.forget(SECOND);

    assertEquals(16L, cache.index().countOf("cobblestone"));
    assertFalse(cache.isTracking(SECOND));
  }

  @Test
  void forgettingTheLastContainerLeavesAnEmptyIndex() {
    containers.put(FIRST, "cobblestone", 16L);
    cache.track(List.of(FIRST));
    cache.index();

    cache.forget(FIRST);

    assertTrue(cache.index().isEmpty());
    assertEquals(0, cache.trackedContainerCount());
  }

  @Test
  void forgettingAContainerRebuildsWithoutReadingAnything() {
    containers.put(FIRST, "cobblestone", 16L);
    containers.put(SECOND, "diamond", 4L);
    cache.track(List.of(FIRST, SECOND));
    cache.index();
    int readsBefore = containers.totalReads();

    cache.forget(SECOND);
    cache.index();

    assertEquals(readsBefore, containers.totalReads());
    assertEquals(2, cache.recomputeCount());
  }

  @Test
  void forgettingAStaleContainerNeverReadsIt() {
    containers.put(FIRST, "cobblestone", 16L);
    containers.put(SECOND, "diamond", 4L);
    cache.track(List.of(FIRST, SECOND));
    cache.index();

    cache.invalidate(SECOND);
    cache.forget(SECOND);
    cache.index();

    assertEquals(1, containers.readsOf(SECOND));
    assertFalse(cache.index().contains("diamond"));
  }

  @Test
  void forgettingAContainerOutsideTheNetworkChangesNothing() {
    containers.put(FIRST, "cobblestone", 16L);
    cache.track(List.of(FIRST));
    cache.index();

    cache.forget(TestOrigins.at(42));

    assertFalse(cache.isStale());
    assertEquals(1, cache.recomputeCount());
    assertEquals(16L, cache.index().countOf("cobblestone"));
  }

  @Test
  void trackingAfreshForgetsThatAnythingWasAdopted() {
    containers.put(FIRST, "cobblestone", 16L);
    cache.track(List.of(FIRST));
    cache.adopt(SECOND);

    cache.track(List.of(FIRST));

    assertFalse(cache.isTracking(SECOND));
  }

  @Test
  void adoptingNothingIsRejected() {
    assertThrows(NullPointerException.class, () -> cache.adopt(null));
  }
}
