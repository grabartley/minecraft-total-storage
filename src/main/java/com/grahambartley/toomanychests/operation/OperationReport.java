package com.grahambartley.toomanychests.operation;

public record OperationReport(
    int stepsDone,
    int stepsTotal,
    int containersSorted,
    int stacksMerged,
    long itemsMoved,
    int excludedSkipped,
    int restrictedSkipped,
    boolean cancelled) {
  public OperationReport {
    if (stepsDone < 0 || stepsTotal < 0) {
      throw new IllegalArgumentException("an operation cannot have taken fewer than no steps");
    }
    if (containersSorted < 0 || stacksMerged < 0 || excludedSkipped < 0 || restrictedSkipped < 0) {
      throw new IllegalArgumentException("an operation cannot count below zero");
    }
    if (itemsMoved < 0L) {
      throw new IllegalArgumentException("itemsMoved must not be negative: " + itemsMoved);
    }
    if (stepsDone > stepsTotal) {
      throw new IllegalArgumentException(
          "an operation took " + stepsDone + " of " + stepsTotal + " steps");
    }
  }

  public static OperationReport nothing() {
    return new OperationReport(0, 0, 0, 0, 0L, 0, 0, false);
  }

  public boolean changedAnything() {
    return itemsMoved > 0L || containersSorted > 0;
  }

  public boolean skippedAnything() {
    return excludedSkipped > 0 || restrictedSkipped > 0;
  }

  public int percentDone() {
    return stepsTotal == 0 ? 100 : (int) Math.floor(stepsDone * 100.0 / stepsTotal);
  }
}
