package com.grahambartley.toomanychests.sort;

import java.util.Objects;

public record StorageSortResult(StorageSortOutcome outcome, int stacksBefore, int stacksAfter) {
  public StorageSortResult {
    Objects.requireNonNull(outcome, "outcome");
    if (stacksBefore < 0 || stacksAfter < 0) {
      throw new IllegalArgumentException("a container cannot hold a negative number of stacks");
    }
    if (stacksAfter > stacksBefore) {
      throw new IllegalArgumentException(
          "sorting turned " + stacksBefore + " stacks into " + stacksAfter);
    }
  }

  public static StorageSortResult of(StorageSortOutcome outcome) {
    return new StorageSortResult(outcome, 0, 0);
  }

  public int stacksMerged() {
    return stacksBefore - stacksAfter;
  }
}
