package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;
import com.grahambartley.totalstorage.operation.OperationConfirmations;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalOperationMirrorTest {
  private static final int SYNC_ID = 4;
  private static final int ANOTHER_SCREEN = 5;

  private static final ExpensiveOperation SORT = ExpensiveOperation.SORT_NETWORK;
  private static final ExpensiveOperation GATHER = ExpensiveOperation.CONSOLIDATE;

  @AfterEach
  void forgetTheOperation() {
    TerminalOperationMirror.clear();
  }

  @Test
  void aTerminalWithNothingGoingOnIsIdle() {
    assertEquals(NetworkOperationPhase.IDLE, TerminalOperationMirror.phaseOf(SYNC_ID, SORT));
    assertEquals(NetworkOperationPhase.IDLE, TerminalOperationMirror.phaseOf(SYNC_ID, GATHER));
  }

  @Test
  void aRunningOperationIsShownOnlyForItsScreenAndItsOperation() {
    TerminalOperationMirror.accept(SYNC_ID, SORT, NetworkOperationPhase.RUNNING, 30);

    assertEquals(NetworkOperationPhase.RUNNING, TerminalOperationMirror.phaseOf(SYNC_ID, SORT));
    assertEquals(NetworkOperationPhase.IDLE, TerminalOperationMirror.phaseOf(SYNC_ID, GATHER));
    assertEquals(NetworkOperationPhase.IDLE, TerminalOperationMirror.phaseOf(ANOTHER_SCREEN, SORT));
  }

  @ParameterizedTest(name = "{0}% shown as {1}%")
  @CsvSource({"-5, 0", "0, 0", "40, 40", "100, 100", "150, 100"})
  void progressStaysBetweenNothingAndDone(int sent, int shown) {
    TerminalOperationMirror.accept(SYNC_ID, SORT, NetworkOperationPhase.RUNNING, sent);

    assertEquals(shown, TerminalOperationMirror.percentDone());
  }

  @Test
  void anArmedOperationTakesOverFromTheOneShownBefore() {
    TerminalOperationMirror.accept(SYNC_ID, SORT, NetworkOperationPhase.RUNNING, 70);

    TerminalOperationMirror.accept(SYNC_ID, GATHER, NetworkOperationPhase.ARMED, 70);

    assertEquals(NetworkOperationPhase.ARMED, TerminalOperationMirror.phaseOf(SYNC_ID, GATHER));
    assertEquals(NetworkOperationPhase.IDLE, TerminalOperationMirror.phaseOf(SYNC_ID, SORT));
  }

  @Test
  void anArmedOperationCountsDownAndThenDisarms() {
    TerminalOperationMirror.accept(SYNC_ID, GATHER, NetworkOperationPhase.ARMED, 0);

    for (int tick = 0; tick < OperationConfirmations.WINDOW_TICKS - 1; tick++) {
      TerminalOperationMirror.tick();
    }
    assertEquals(NetworkOperationPhase.ARMED, TerminalOperationMirror.phaseOf(SYNC_ID, GATHER));

    TerminalOperationMirror.tick();

    assertEquals(NetworkOperationPhase.IDLE, TerminalOperationMirror.phaseOf(SYNC_ID, GATHER));
  }

  @Test
  void aRunningOperationDoesNotTimeOut() {
    TerminalOperationMirror.accept(SYNC_ID, SORT, NetworkOperationPhase.RUNNING, 10);

    for (int tick = 0; tick < OperationConfirmations.WINDOW_TICKS * 2; tick++) {
      TerminalOperationMirror.tick();
    }

    assertEquals(NetworkOperationPhase.RUNNING, TerminalOperationMirror.phaseOf(SYNC_ID, SORT));
  }

  @Test
  void goingIdleClearsTheOperation() {
    TerminalOperationMirror.accept(SYNC_ID, SORT, NetworkOperationPhase.RUNNING, 80);

    TerminalOperationMirror.accept(SYNC_ID, SORT, NetworkOperationPhase.IDLE, 100);

    assertEquals(NetworkOperationPhase.IDLE, TerminalOperationMirror.phaseOf(SYNC_ID, SORT));
  }

  @Test
  void anotherOperationGoingIdleLeavesTheShownOneAlone() {
    TerminalOperationMirror.accept(SYNC_ID, SORT, NetworkOperationPhase.RUNNING, 50);

    TerminalOperationMirror.accept(SYNC_ID, GATHER, NetworkOperationPhase.IDLE, 0);

    assertEquals(NetworkOperationPhase.RUNNING, TerminalOperationMirror.phaseOf(SYNC_ID, SORT));
    assertEquals(50, TerminalOperationMirror.percentDone());
  }

  @Test
  void anUpdateWithoutAnOperationOrPhaseIsIgnored() {
    TerminalOperationMirror.accept(SYNC_ID, SORT, NetworkOperationPhase.RUNNING, 50);

    TerminalOperationMirror.accept(SYNC_ID, null, NetworkOperationPhase.IDLE, 0);
    TerminalOperationMirror.accept(SYNC_ID, SORT, null, 0);

    assertEquals(NetworkOperationPhase.RUNNING, TerminalOperationMirror.phaseOf(SYNC_ID, SORT));
  }

  @Test
  void tickingWithNothingArmedIsHarmless() {
    TerminalOperationMirror.tick();

    assertEquals(NetworkOperationPhase.IDLE, TerminalOperationMirror.phaseOf(SYNC_ID, SORT));
  }
}
