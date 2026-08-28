package com.grahambartley.toomanychests.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class TransactionRunnerTest {
  private static final String DIAMOND = "diamond";

  private final TransactionRunner runner = new TransactionRunner(() -> true);

  @Test
  void aCommittedOperationKeepsWhatItMoved() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 30);
    FakeContainer to = FakeContainer.withSlots(1, 64);

    OperationResult<Long> result =
        runner.run(
            transaction ->
                OperationResult.committed(
                    StorageTransfers.move(from, to, DIAMOND, 30, transaction)));

    assertTrue(result.committed());
    assertEquals(30L, result.orElse(0L));
    assertEquals(30L, to.amountOf(DIAMOND));
    assertEquals(0L, from.amountOf(DIAMOND));
  }

  @Test
  void aRolledBackResultUndoesEverythingTheOperationDid() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 30);
    FakeContainer to = FakeContainer.withSlots(1, 64);
    Map<Integer, FakeContainer.ResourceEntry> before = from.contents();

    OperationResult<Long> result =
        runner.run(
            transaction -> {
              StorageTransfers.move(from, to, DIAMOND, 30, transaction);
              return OperationResult.rolledBack("changed its mind");
            });

    assertFalse(result.committed());
    assertEquals("changed its mind", result.reason());
    assertEquals(before, from.contents());
    assertEquals(0L, to.total());
  }

  @Test
  void anOperationThatThrowsRollsBackAndReportsFailureRatherThanEscaping() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 30);
    FakeContainer to = FakeContainer.withSlots(1, 64);

    OperationResult<Long> result =
        runner.run(
            transaction -> {
              StorageTransfers.move(from, to, DIAMOND, 30, transaction);
              throw new IllegalStateException("container vanished");
            });

    assertFalse(result.committed());
    assertEquals("operation threw IllegalStateException", result.reason());
    assertEquals(30L, from.amountOf(DIAMOND));
    assertEquals(0L, to.total());
  }

  @Test
  void anOperationReturningNoResultRollsBack() {
    FakeContainer from = FakeContainer.withSlots(1, 64).holding(DIAMOND, 30);
    FakeContainer to = FakeContainer.withSlots(1, 64);

    OperationResult<Long> result =
        runner.run(
            transaction -> {
              StorageTransfers.move(from, to, DIAMOND, 30, transaction);
              return null;
            });

    assertFalse(result.committed());
    assertEquals("operation returned no result", result.reason());
    assertEquals(30L, from.amountOf(DIAMOND));
    assertEquals(0L, to.total());
  }

  @Test
  void anOperationOffTheServerThreadIsRefusedBeforeItRuns() {
    AtomicBoolean ran = new AtomicBoolean();
    TransactionRunner offThread = new TransactionRunner(() -> false);

    assertThrows(
        IllegalStateException.class,
        () ->
            offThread.run(
                transaction -> {
                  ran.set(true);
                  return OperationResult.committed(1L);
                }));

    assertFalse(ran.get());
  }

  @Test
  void aSecondOuterTransactionOnTheSameThreadIsRefused() {
    OperationResult<Long> result =
        runner.run(
            transaction -> {
              assertThrows(
                  IllegalStateException.class,
                  () -> runner.run(inner -> OperationResult.committed(1L)));
              return OperationResult.committed(1L);
            });

    assertTrue(result.committed());
  }

  @Test
  void aFailedStepLeavesTheStepsBeforeItIntactAndCommitsWithTheParent() {
    FakeContainer first = FakeContainer.withSlots(1, 64).holding(DIAMOND, 20);
    FakeContainer second = FakeContainer.withSlots(1, 64).holding(DIAMOND, 20);
    FakeContainer to = FakeContainer.withSlots(1, 64);

    OperationResult<Long> result =
        runner.run(
            transaction -> {
              OperationResult<Long> good =
                  runner.runNested(
                      transaction,
                      step ->
                          OperationResult.committed(
                              StorageTransfers.move(first, to, DIAMOND, 20, step)));
              OperationResult<Long> bad =
                  runner.runNested(
                      transaction,
                      step -> {
                        StorageTransfers.move(second, to, DIAMOND, 20, step);
                        return OperationResult.rolledBack("second step failed");
                      });
              assertTrue(good.committed());
              assertFalse(bad.committed());
              return OperationResult.committed(good.orElse(0L));
            });

    assertTrue(result.committed());
    assertEquals(0L, first.amountOf(DIAMOND));
    assertEquals(20L, second.amountOf(DIAMOND));
    assertEquals(20L, to.amountOf(DIAMOND));
  }

  @Test
  void abandoningTheParentUndoesEveryStepThatSucceeded() {
    FakeContainer first = FakeContainer.withSlots(1, 64).holding(DIAMOND, 20);
    FakeContainer second = FakeContainer.withSlots(1, 64).holding(DIAMOND, 20);
    FakeContainer to = FakeContainer.withSlots(1, 64);

    OperationResult<Long> result =
        runner.run(
            transaction -> {
              runner.runNested(
                  transaction,
                  step ->
                      OperationResult.committed(
                          StorageTransfers.move(first, to, DIAMOND, 20, step)));
              runner.runNested(
                  transaction,
                  step ->
                      OperationResult.committed(
                          StorageTransfers.move(second, to, DIAMOND, 20, step)));
              return OperationResult.rolledBack("the whole move is off");
            });

    assertFalse(result.committed());
    assertEquals(20L, first.amountOf(DIAMOND));
    assertEquals(20L, second.amountOf(DIAMOND));
    assertEquals(0L, to.total());
  }
}
