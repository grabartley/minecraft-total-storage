package com.grahambartley.totalstorage.operation;

import java.util.Objects;

public record QuickStackOutcome(QuickStackResult result, long moved, int containersTouched) {
  public QuickStackOutcome {
    Objects.requireNonNull(result, "result");
    if (moved < 0L) {
      throw new IllegalArgumentException("moved must not be negative: " + moved);
    }
    if (containersTouched < 0) {
      throw new IllegalArgumentException(
          "containersTouched must not be negative: " + containersTouched);
    }
    if (result.movedAnything() != (moved > 0L)) {
      throw new IllegalArgumentException(result + " does not describe moving " + moved);
    }
    if (moved > 0L && containersTouched == 0) {
      throw new IllegalArgumentException("items moved into no containers at all");
    }
  }

  public static QuickStackOutcome refused() {
    return new QuickStackOutcome(QuickStackResult.REFUSED, 0L, 0);
  }

  public static QuickStackOutcome rateLimited() {
    return new QuickStackOutcome(QuickStackResult.RATE_LIMITED, 0L, 0);
  }

  public static QuickStackOutcome nothingToStack() {
    return new QuickStackOutcome(QuickStackResult.NOTHING_TO_STACK, 0L, 0);
  }

  public static QuickStackOutcome stacked(long moved, int containersTouched) {
    return new QuickStackOutcome(QuickStackResult.STACKED, moved, containersTouched);
  }
}
