package com.grahambartley.totalstorage.index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.grahambartley.totalstorage.mutation.ContainerMutationRegistry;
import com.grahambartley.totalstorage.storage.BlockStorageOrigin;
import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NetworkIndexSessionTest {
  private static final Comparator<NetworkIndexEntry<String>> BY_NAME =
      NetworkIndexOrdering.byKey(Function.identity());
  private static final StorageOrigin FIRST = TestOrigins.at(0);
  private static final StorageOrigin SECOND = TestOrigins.at(1);
  private static final StorageOrigin OUTSIDE = TestOrigins.at(99);

  private final ContainerMutationRegistry registry = new ContainerMutationRegistry();
  private final FakeContainers containers = new FakeContainers();
  private final NetworkIndexSession<String> session =
      new NetworkIndexSession<>(registry, containers, BY_NAME);

  @Test
  void anUnpoweredSessionRegistersNoListenersAndIndexesNothing() {
    containers.put(FIRST, "cobblestone", 64L);

    assertFalse(session.isActive());
    assertTrue(session.index().isEmpty());
    assertTrue(registry.isIdle());
    assertEquals(0, containers.totalReads());
  }

  @Test
  void poweringOnListensToExactlyWhatTheScanFound() {
    session.begin(List.of(FIRST, SECOND));

    assertTrue(session.isActive());
    assertEquals(2, registry.watchedContainerCount());
    assertTrue(registry.isWatched(FIRST));
    assertTrue(registry.isWatched(SECOND));
    assertFalse(registry.isWatched(OUTSIDE));
  }

  @Test
  void poweringOnDoesNoWorkUntilTheIndexIsRead() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));

    assertEquals(0, containers.totalReads());
    assertEquals(0, session.recomputeCount());
  }

  @Test
  void aMutationInAWatchedContainerShowsUpOnTheNextRead() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));
    session.index();

    containers.put(FIRST, "cobblestone", 128L);
    registry.publish(TestOrigins.OVERWORLD, positionOf(FIRST));

    assertEquals(128L, session.index().countOf("cobblestone"));
  }

  @Test
  void aMutationInAContainerOutsideTheNetworkChangesNothing() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));
    session.index();

    registry.publish(TestOrigins.OVERWORLD, positionOf(OUTSIDE));
    session.index();

    assertEquals(1, session.recomputeCount());
    assertEquals(1, session.containerReadCount());
  }

  @ParameterizedTest
  @ValueSource(ints = {8, 64, 256})
  void aBurstOfMutationsBeforeOneReadRebuildsTheIndexOnceOnly(int mutations) {
    containers.put(FIRST, "cobblestone", 1L);
    containers.put(SECOND, "diamond", 8L);
    session.begin(List.of(FIRST, SECOND));
    session.index();

    for (int mutation = 1; mutation <= mutations; mutation++) {
      containers.put(FIRST, "cobblestone", (long) mutation);
      registry.publish(TestOrigins.OVERWORLD, positionOf(FIRST));
    }
    session.index();

    assertEquals(2, session.recomputeCount());
    assertEquals(3, session.containerReadCount());
  }

  @Test
  void aWatchedContainerChangingNeverReReadsTheRestOfTheNetwork() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    session.begin(List.of(FIRST, SECOND));
    session.index();

    for (int mutation = 0; mutation < 20; mutation++) {
      registry.publish(TestOrigins.OVERWORLD, positionOf(FIRST));
      session.index();
    }

    assertEquals(1, containers.readsOf(SECOND));
    assertEquals(21, containers.readsOf(FIRST));
  }

  @Test
  void aPoweredSessionNobodyChangesDoesNoWorkHoweverLongItRuns() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));
    session.index();

    for (int tick = 0; tick < 500; tick++) {
      session.index();
    }

    assertEquals(1, session.recomputeCount());
    assertEquals(1, session.containerReadCount());
  }

  @Test
  void poweringOffRemovesEveryListener() {
    session.begin(List.of(FIRST, SECOND));

    session.end();

    assertTrue(registry.isIdle());
    assertFalse(session.isActive());
    assertTrue(session.watchedContainers().isEmpty());
  }

  @Test
  void poweringOffLeavesNothingToIndex() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));
    session.index();

    session.end();

    assertTrue(session.index().isEmpty());
  }

  @Test
  void aMutationAfterPoweringOffIsNotEvenDelivered() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));
    session.index();
    session.end();

    registry.publish(TestOrigins.OVERWORLD, positionOf(FIRST));
    session.index();

    assertEquals(1, session.recomputeCount());
  }

  @Test
  void poweringOffOneTerminalLeavesAnotherWatchingTheSameContainer() {
    NetworkIndexSession<String> other = new NetworkIndexSession<>(registry, containers, BY_NAME);
    session.begin(List.of(FIRST));
    other.begin(List.of(FIRST));

    session.end();

    assertTrue(registry.isWatched(FIRST));
    assertTrue(other.isActive());
  }

  @Test
  void rescanningReplacesTheWatchedSetRatherThanAddingToIt() {
    session.begin(List.of(FIRST));

    session.begin(List.of(SECOND));

    assertEquals(List.of(SECOND), session.watchedContainers());
    assertEquals(1, registry.watchedContainerCount());
    assertFalse(registry.isWatched(FIRST));
  }

  @Test
  void aPoweredSessionWithNothingInRangeIsStillPowered() {
    session.begin(List.of());

    assertTrue(session.isActive());
    assertTrue(session.index().isEmpty());
    assertTrue(registry.isIdle());
  }

  @Test
  void admittingAContainerMidSessionListensToItAndIndexesIt() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    session.begin(List.of(FIRST));
    session.index();

    session.admit(SECOND);

    assertEquals(8L, session.index().countOf("diamond"));
    assertTrue(session.isWatching(SECOND));
    assertTrue(registry.isWatched(SECOND));
  }

  @Test
  void admittingAContainerWhileUnpoweredListensToNothing() {
    containers.put(FIRST, "cobblestone", 64L);

    session.admit(FIRST);

    assertFalse(session.isWatching(FIRST));
    assertTrue(registry.isIdle());
    assertTrue(session.index().isEmpty());
  }

  @Test
  void admittingAContainerAlreadyWatchedNeitherDuplicatesItNorReReadsIt() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));
    session.index();

    session.admit(FIRST);
    session.index();

    assertEquals(List.of(FIRST), session.watchedContainers());
    assertEquals(1, session.containerReadCount());
  }

  @Test
  void droppingAContainerStopsListeningAndTakesItsItemsOutOfTheListing() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    session.begin(List.of(FIRST, SECOND));
    session.index();

    session.drop(SECOND);

    assertEquals(0L, session.index().countOf("diamond"));
    assertEquals(64L, session.index().countOf("cobblestone"));
    assertFalse(registry.isWatched(SECOND));
    assertFalse(session.isWatching(SECOND));
  }

  @Test
  void droppingAContainerLeavesTheRestOfTheNetworkUnread() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    session.begin(List.of(FIRST, SECOND));
    session.index();

    session.drop(SECOND);
    session.index();

    assertEquals(2, session.containerReadCount());
  }

  @Test
  void droppingAContainerThisSessionNeverHeldLeavesAnotherWatcherAlone() {
    NetworkIndexSession<String> other = new NetworkIndexSession<>(registry, containers, BY_NAME);
    other.begin(List.of(OUTSIDE));
    session.begin(List.of(FIRST));

    session.drop(OUTSIDE);

    assertTrue(registry.isWatched(OUTSIDE));
    assertTrue(other.isWatching(OUTSIDE));
  }

  @Test
  void unloadingAChunkDropsEveryContainerInItAndNothingElse() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    containers.put(OUTSIDE, "torch", 2L);
    session.begin(List.of(FIRST, SECOND, OUTSIDE));
    session.index();

    List<StorageOrigin> dropped = session.dropChunk(TestOrigins.OVERWORLD, new ChunkPos(0, 0));

    assertEquals(List.of(FIRST, SECOND), dropped);
    assertEquals(List.of(OUTSIDE), session.watchedContainers());
    assertEquals(2L, session.index().countOf("torch"));
    assertEquals(0L, session.index().countOf("cobblestone"));
  }

  @Test
  void unloadingAChunkHoldingNothingWatchedDropsNothing() {
    session.begin(List.of(FIRST));

    assertTrue(session.dropChunk(TestOrigins.OVERWORLD, new ChunkPos(9, 9)).isEmpty());
    assertTrue(session.isWatching(FIRST));
  }

  @Test
  void unloadingAChunkInAnotherWorldDropsNothing() {
    session.begin(List.of(FIRST));

    List<StorageOrigin> dropped = session.dropChunk(TestOrigins.NETHER, new ChunkPos(0, 0));

    assertTrue(dropped.isEmpty());
    assertTrue(session.isWatching(FIRST));
  }

  @Test
  void unloadingAChunkWhileUnpoweredDropsNothing() {
    assertTrue(session.dropChunk(TestOrigins.OVERWORLD, new ChunkPos(0, 0)).isEmpty());
  }

  @Test
  void reconcilingAdmitsWhatIsNewAndDropsWhatIsGone() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    session.begin(List.of(FIRST));
    session.index();

    session.reconcile(List.of(SECOND));

    assertEquals(List.of(SECOND), session.watchedContainers());
    assertEquals(8L, session.index().countOf("diamond"));
    assertEquals(0L, session.index().countOf("cobblestone"));
    assertFalse(registry.isWatched(FIRST));
  }

  @Test
  void reconcilingReReadsOnlyTheContainersItAdmitted() {
    containers.put(FIRST, "cobblestone", 64L);
    containers.put(SECOND, "diamond", 8L);
    session.begin(List.of(FIRST));
    session.index();

    session.reconcile(List.of(FIRST, SECOND));
    session.index();

    assertEquals(1, containers.readsOf(FIRST));
    assertEquals(1, containers.readsOf(SECOND));
  }

  @Test
  void reconcilingWithTheSameContainersChangesNothing() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));
    session.index();

    session.reconcile(List.of(FIRST));
    session.index();

    assertEquals(1, session.recomputeCount());
    assertEquals(1, session.containerReadCount());
  }

  @Test
  void reconcilingWithNothingLeavesAPoweredSessionHoldingNothing() {
    containers.put(FIRST, "cobblestone", 64L);
    session.begin(List.of(FIRST));
    session.index();

    session.reconcile(List.of());

    assertTrue(session.isActive());
    assertTrue(session.index().isEmpty());
    assertTrue(registry.isIdle());
  }

  @Test
  void reconcilingWhileUnpoweredAdoptsNothing() {
    containers.put(FIRST, "cobblestone", 64L);

    session.reconcile(List.of(FIRST));

    assertFalse(session.isActive());
    assertTrue(registry.isIdle());
  }

  @Test
  void membershipChangesRejectMissingOrigins() {
    session.begin(List.of(FIRST));

    assertThrows(NullPointerException.class, () -> session.admit(null));
    assertThrows(NullPointerException.class, () -> session.reconcile((List<StorageOrigin>) null));
  }

  private static BlockPos positionOf(StorageOrigin origin) {
    return ((BlockStorageOrigin) origin).pos();
  }
}
