package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.sort.StorageSortOutcome;
import com.grahambartley.toomanychests.sort.StorageSortResult;
import java.util.Objects;

public final class OperationTally {
  private int stepsTotal;
  private int stepsDone;
  private int containersSorted;
  private int stacksMerged;
  private long itemsMoved;
  private int excludedSkipped;
  private int restrictedSkipped;
  private boolean cancelled;

  public OperationTally(int stepsTotal) {
    if (stepsTotal < 0) {
      throw new IllegalArgumentException("stepsTotal must not be negative: " + stepsTotal);
    }
    this.stepsTotal = stepsTotal;
  }

  public void finished() {
    stepsDone = stepsTotal;
  }

  public void stepped() {
    stepsDone = Math.min(stepsTotal, stepsDone + 1);
  }

  public void sorted(StorageSortResult result) {
    Objects.requireNonNull(result, "result");
    if (result.outcome().wasSorted()) {
      containersSorted++;
      stacksMerged += result.stacksMerged();
      return;
    }
    if (result.outcome() == StorageSortOutcome.EXCLUDED) {
      excludedSkipped++;
    }
    if (result.outcome() == StorageSortOutcome.NOT_SORTABLE) {
      restrictedSkipped++;
    }
  }

  public void moved(long items) {
    if (items > 0L) {
      itemsMoved += items;
    }
  }

  public void cancelled() {
    cancelled = true;
  }

  public boolean wasCancelled() {
    return cancelled;
  }

  public OperationReport report() {
    return new OperationReport(
        stepsDone,
        stepsTotal,
        containersSorted,
        stacksMerged,
        itemsMoved,
        excludedSkipped,
        restrictedSkipped,
        cancelled);
  }
}
