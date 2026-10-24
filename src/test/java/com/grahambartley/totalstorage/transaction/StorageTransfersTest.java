package com.grahambartley.totalstorage.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class StorageTransfersTest {
  private static final String DIAMOND = "diamond";
  private static final String COAL = "coal";

  @ParameterizedTest
  @CsvSource({"64, 64, 64", "64, 20, 20", "20, 64, 20", "0, 10, 0"})
  void aMoveTakesWhatBothTheSourceAndTheRequestAllow(long held, long requested, long expected) {
    FakeContainer from = FakeContainer.withSlots(3, 64).holding(DIAMOND, held);
    FakeContainer to = FakeContainer.withSlots(3, 64);

    long moved =
        commit(transaction -> StorageTransfers.move(from, to, DIAMOND, requested, transaction));

    assertEquals(expected, moved);
    assertEquals(held - expected, from.amountOf(DIAMOND));
    assertEquals(expected, to.amountOf(DIAMOND));
  }

  @Test
  void aMoveIntoAFullDestinationLeavesTheSourceExactlyAsItWas() {
    FakeContainer from = FakeContainer.withSlots(3, 64).holding(DIAMOND, 64);
    FakeContainer to = FakeContainer.withSlots(1, 64).holding(COAL, 64);
    Map<Integer, FakeContainer.ResourceEntry> before = from.contents();

    long moved = commit(transaction -> StorageTransfers.move(from, to, DIAMOND, 64, transaction));

    assertEquals(0L, moved);
    assertEquals(before, from.contents());
    assertEquals(64L, to.amountOf(COAL));
  }

  @Test
  void aMoveTakesOnlyWhatTheDestinationCanActuallyHold() {
    FakeContainer from = FakeContainer.withSlots(3, 64).holding(DIAMOND, 64);
    FakeContainer to = FakeContainer.withSlots(1, 64).holding(DIAMOND, 60);

    long moved = commit(transaction -> StorageTransfers.move(from, to, DIAMOND, 64, transaction));

    assertEquals(4L, moved);
    assertEquals(60L, from.amountOf(DIAMOND));
    assertEquals(64L, to.amountOf(DIAMOND));
  }

  @Test
  void anExactMoveThatTheDestinationCannotTakeInFullMovesNothing() {
    FakeContainer from = FakeContainer.withSlots(3, 64).holding(DIAMOND, 64);
    FakeContainer to = FakeContainer.withSlots(1, 64).holding(DIAMOND, 60);
    Map<Integer, FakeContainer.ResourceEntry> fromBefore = from.contents();
    Map<Integer, FakeContainer.ResourceEntry> toBefore = to.contents();

    boolean moved =
        commit(transaction -> StorageTransfers.moveExactly(from, to, DIAMOND, 64, transaction));

    assertFalse(moved);
    assertEquals(fromBefore, from.contents());
    assertEquals(toBefore, to.contents());
  }

  @Test
  void anExactMoveThatFitsMovesTheWholeAmount() {
    FakeContainer from = FakeContainer.withSlots(3, 64).holding(DIAMOND, 64);
    FakeContainer to = FakeContainer.withSlots(3, 64);

    boolean moved =
        commit(transaction -> StorageTransfers.moveExactly(from, to, DIAMOND, 40, transaction));

    assertTrue(moved);
    assertEquals(24L, from.amountOf(DIAMOND));
    assertEquals(40L, to.amountOf(DIAMOND));
  }

  @Test
  void aGatherPullsFromEachContainerInTurnUntilTheRequestIsMet() {
    FakeContainer first = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer second = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer third = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer to = FakeContainer.withSlots(3, 64);

    long moved =
        commit(
            transaction ->
                StorageTransfers.gather(
                    List.of(first, second, third), to, DIAMOND, 25, transaction));

    assertEquals(25L, moved);
    assertEquals(0L, first.amountOf(DIAMOND));
    assertEquals(0L, second.amountOf(DIAMOND));
    assertEquals(5L, third.amountOf(DIAMOND));
    assertEquals(25L, to.amountOf(DIAMOND));
  }

  @Test
  void anExactGatherTheNetworkCannotSatisfyLeavesEveryContainerUntouched() {
    FakeContainer first = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer second = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer third = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer to = FakeContainer.withSlots(3, 64);
    List<Map<Integer, FakeContainer.ResourceEntry>> before =
        List.of(first.contents(), second.contents(), third.contents(), to.contents());

    boolean moved =
        commit(
            transaction ->
                StorageTransfers.gatherExactly(
                    List.of(first, second, third), to, DIAMOND, 35, transaction));

    assertFalse(moved);
    assertEquals(
        before, List.of(first.contents(), second.contents(), third.contents(), to.contents()));
  }

  @Test
  void anExactGatherAcrossContainersCommitsEveryHopTogether() {
    FakeContainer first = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer second = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer to = FakeContainer.withSlots(3, 64);

    boolean moved =
        commit(
            transaction ->
                StorageTransfers.gatherExactly(
                    List.of(first, second), to, DIAMOND, 20, transaction));

    assertTrue(moved);
    assertEquals(0L, first.amountOf(DIAMOND));
    assertEquals(0L, second.amountOf(DIAMOND));
    assertEquals(20L, to.amountOf(DIAMOND));
  }

  @Test
  void aCommittedMoveDoesNotSurviveAnAbortedOuterTransaction() {
    FakeContainer from = FakeContainer.withSlots(3, 64).holding(DIAMOND, 64);
    FakeContainer to = FakeContainer.withSlots(3, 64);
    Map<Integer, FakeContainer.ResourceEntry> before = from.contents();

    try (Transaction outer = Transaction.openOuter()) {
      assertEquals(40L, StorageTransfers.move(from, to, DIAMOND, 40, outer));
      outer.abort();
    }

    assertEquals(before, from.contents());
    assertEquals(0L, to.total());
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, 5L, 30L, 64L, 200L})
  void nothingIsCreatedOrDestroyedWhateverTheRequest(long requested) {
    FakeContainer from = FakeContainer.withSlots(2, 64).holding(DIAMOND, 70);
    FakeContainer to = FakeContainer.withSlots(1, 64).holding(DIAMOND, 50);
    long before = from.total() + to.total();

    commit(transaction -> StorageTransfers.move(from, to, DIAMOND, requested, transaction));

    assertEquals(before, from.total() + to.total());
  }

  @Test
  void aNegativeRequestIsRejectedRatherThanTreatedAsZero() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer to = FakeContainer.withSlots(1, 64);

    assertThrows(
        IllegalArgumentException.class,
        () -> commit(transaction -> StorageTransfers.move(from, to, DIAMOND, -1, transaction)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            commit(
                transaction ->
                    StorageTransfers.gather(List.of(from), to, DIAMOND, -1, transaction)));
  }

  @ParameterizedTest
  @ValueSource(longs = {-1L, Long.MIN_VALUE})
  void spreadingANegativeAmountIsRefusedBeforeAnythingMoves(long amount) {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer to = FakeContainer.withSlots(1, 64);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            commit(
                transaction ->
                    StorageTransfers.distribute(from, List.of(to), DIAMOND, amount, transaction)));
    assertEquals(10L, from.amountOf(DIAMOND));
  }

  @ParameterizedTest
  @ValueSource(longs = {0L, -1L})
  void anExactMoveOfNothingIsRejected(long amount) {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer to = FakeContainer.withSlots(1, 64);

    assertThrows(
        IllegalArgumentException.class,
        () ->
            commit(
                transaction ->
                    StorageTransfers.moveExactly(from, to, DIAMOND, amount, transaction)));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            commit(
                transaction ->
                    StorageTransfers.gatherExactly(
                        List.<Storage<String>>of(from), to, DIAMOND, amount, transaction)));
  }

  @Test
  void aDistributeFillsEachDestinationInTurnUntilTheSourceIsEmpty() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 64);
    FakeContainer first = FakeContainer.withSlots(1, 64).holding(DIAMOND, 44);
    FakeContainer second = FakeContainer.withSlots(1, 64);

    long moved =
        commit(
            transaction ->
                StorageTransfers.distribute(
                    from, List.of(first, second), DIAMOND, 64, transaction));

    assertEquals(64L, moved);
    assertEquals(0L, from.amountOf(DIAMOND));
    assertEquals(64L, first.amountOf(DIAMOND));
    assertEquals(44L, second.amountOf(DIAMOND));
  }

  @Test
  void aDistributeRespectsTheOrderItWasGiven() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 10);
    FakeContainer preferred = FakeContainer.withSlots(1, 64);
    FakeContainer fallback = FakeContainer.withSlots(1, 64);

    commit(
        transaction ->
            StorageTransfers.distribute(
                from, List.of(preferred, fallback), DIAMOND, 10, transaction));

    assertEquals(10L, preferred.amountOf(DIAMOND));
    assertEquals(0L, fallback.amountOf(DIAMOND));
  }

  @Test
  void aDistributeWithNowhereToPutAnythingLeavesTheSourceExactlyAsItWas() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 64);
    FakeContainer full = FakeContainer.withSlots(1, 64).holding(COAL, 64);
    Map<Integer, FakeContainer.ResourceEntry> before = from.contents();

    long moved =
        commit(
            transaction ->
                StorageTransfers.distribute(from, List.of(full), DIAMOND, 64, transaction));

    assertEquals(0L, moved);
    assertEquals(before, from.contents());
  }

  @Test
  void aDistributeStopsAtTheAmountItWasAskedFor() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 64);
    FakeContainer to = FakeContainer.withSlots(2, 64);

    long moved =
        commit(
            transaction ->
                StorageTransfers.distribute(from, List.of(to), DIAMOND, 20, transaction));

    assertEquals(20L, moved);
    assertEquals(44L, from.amountOf(DIAMOND));
  }

  @Test
  void aDistributeToNowhereAtAllMovesNothing() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 64);

    long moved =
        commit(
            transaction ->
                StorageTransfers.distribute(
                    from, List.<Storage<String>>of(), DIAMOND, 64, transaction));

    assertEquals(0L, moved);
    assertEquals(64L, from.amountOf(DIAMOND));
  }

  private static <T> T commit(java.util.function.Function<Transaction, T> work) {
    try (Transaction outer = Transaction.openOuter()) {
      T result = work.apply(outer);
      outer.commit();
      return result;
    }
  }
}
