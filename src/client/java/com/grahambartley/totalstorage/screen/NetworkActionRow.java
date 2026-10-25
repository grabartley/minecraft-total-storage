package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.operation.ExpensiveOperation;
import com.grahambartley.totalstorage.operation.NetworkOperationPhase;

public record NetworkActionRow(Mode mode, ExpensiveOperation operation, int percentDone) {
  public enum Mode {
    SPLIT,

    ARMED,

    RUNNING
  }

  public static NetworkActionRow of(
      NetworkOperationPhase sort, NetworkOperationPhase gather, int percentDone) {
    if (sort == NetworkOperationPhase.RUNNING) {
      return new NetworkActionRow(Mode.RUNNING, ExpensiveOperation.SORT_NETWORK, percentDone);
    }
    if (gather == NetworkOperationPhase.RUNNING) {
      return new NetworkActionRow(Mode.RUNNING, ExpensiveOperation.CONSOLIDATE, percentDone);
    }
    if (sort == NetworkOperationPhase.ARMED) {
      return new NetworkActionRow(Mode.ARMED, ExpensiveOperation.SORT_NETWORK, 0);
    }
    if (gather == NetworkOperationPhase.ARMED) {
      return new NetworkActionRow(Mode.ARMED, ExpensiveOperation.CONSOLIDATE, 0);
    }
    return new NetworkActionRow(Mode.SPLIT, null, 0);
  }

  public boolean isSplit() {
    return mode == Mode.SPLIT;
  }
}
