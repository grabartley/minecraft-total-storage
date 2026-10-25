package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;
import com.grahambartley.totalstorage.operation.OperationConfirmations;

public final class TerminalOperationMirror {
  private static final int NO_SCREEN = -1;

  private static int syncId = NO_SCREEN;
  private static ExpensiveOperation operation;
  private static NetworkOperationPhase phase = NetworkOperationPhase.IDLE;
  private static int percentDone;
  private static int armedTicksLeft;

  private TerminalOperationMirror() {}

  public static void accept(
      int shownSyncId, ExpensiveOperation shown, NetworkOperationPhase shownPhase, int percent) {
    if (shown == null || shownPhase == null) {
      return;
    }
    if (shownPhase == NetworkOperationPhase.IDLE && operation != null && shown != operation) {
      return;
    }
    syncId = shownSyncId;
    operation = shownPhase == NetworkOperationPhase.IDLE ? null : shown;
    phase = shownPhase;
    percentDone = Math.max(0, Math.min(100, percent));
    armedTicksLeft =
        shownPhase == NetworkOperationPhase.ARMED ? OperationConfirmations.WINDOW_TICKS : 0;
  }

  public static void tick() {
    if (phase != NetworkOperationPhase.ARMED) {
      return;
    }
    armedTicksLeft--;
    if (armedTicksLeft <= 0) {
      clear();
    }
  }

  public static void clear() {
    syncId = NO_SCREEN;
    operation = null;
    phase = NetworkOperationPhase.IDLE;
    percentDone = 0;
    armedTicksLeft = 0;
  }

  public static NetworkOperationPhase phaseOf(int openSyncId, ExpensiveOperation asked) {
    return syncId == openSyncId && asked == operation ? phase : NetworkOperationPhase.IDLE;
  }

  public static boolean isRunningOn(int openSyncId) {
    return syncId == openSyncId && phase == NetworkOperationPhase.RUNNING;
  }

  public static ExpensiveOperation operation() {
    return operation;
  }

  public static int percentDone() {
    return percentDone;
  }

  public static int armedTicksLeft() {
    return armedTicksLeft;
  }
}
