package com.grahambartley.totalstorage.operation;

import java.util.Objects;

public record WithdrawOutcome(WithdrawResult result, long requested, long moved) {
  public WithdrawOutcome {
    Objects.requireNonNull(result, "result");
    if (requested < 0L) {
      throw new IllegalArgumentException("requested must not be negative: " + requested);
    }
    if (moved < 0L) {
      throw new IllegalArgumentException("moved must not be negative: " + moved);
    }
    if (moved > requested) {
      throw new IllegalArgumentException("a withdrawal cannot move more than it asked for");
    }
    if (result.movedAnything() != moved > 0L) {
      throw new IllegalArgumentException(result + " does not describe moving " + moved);
    }
  }

  public static WithdrawOutcome refused() {
    return new WithdrawOutcome(WithdrawResult.REFUSED, 0L, 0L);
  }

  public static WithdrawOutcome filled(long requested) {
    return new WithdrawOutcome(WithdrawResult.WITHDRAWN, requested, requested);
  }

  public static WithdrawOutcome nothingMoved(long requested, ShortfallCause cause) {
    Objects.requireNonNull(cause, "cause");
    return new WithdrawOutcome(cause.emptyHanded(), requested, 0L);
  }

  public static WithdrawOutcome partial(long requested, long moved, ShortfallCause cause) {
    Objects.requireNonNull(cause, "cause");
    if (moved <= 0L) {
      throw new IllegalArgumentException("a partial withdrawal has to have moved something");
    }
    if (moved >= requested) {
      throw new IllegalArgumentException("a withdrawal that met its request is not partial");
    }
    return new WithdrawOutcome(cause.partial(), requested, moved);
  }

  public long shortfall() {
    return requested - moved;
  }
}
