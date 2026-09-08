package com.grahambartley.toomanychests.operation;

import java.util.Objects;

/**
 * What one withdrawal did: how much was asked for, and how much really moved.
 *
 * <p>Both numbers are carried because the difference between them is the whole message. A player
 * who clicked for a stack and received nine needs to be told nine, and a caller deciding whether to
 * resync a screen needs to know something moved at all.
 *
 * @param result how the withdrawal ended
 * @param requested the amount the request was bounded to, after clamping
 * @param moved how much really reached the player
 */
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

  public static WithdrawOutcome nothingAvailable(long requested) {
    return new WithdrawOutcome(WithdrawResult.NOTHING_AVAILABLE, requested, 0L);
  }

  public static WithdrawOutcome noRoom(long requested) {
    return new WithdrawOutcome(WithdrawResult.NO_ROOM, requested, 0L);
  }

  /** Decides between a whole and a partial withdrawal from the amounts themselves. */
  public static WithdrawOutcome moved(long requested, long moved) {
    if (moved <= 0L) {
      throw new IllegalArgumentException("a withdrawal that moved nothing needs a reason");
    }
    return new WithdrawOutcome(
        moved >= requested ? WithdrawResult.WITHDRAWN : WithdrawResult.PARTIAL, requested, moved);
  }

  public long shortfall() {
    return requested - moved;
  }
}
