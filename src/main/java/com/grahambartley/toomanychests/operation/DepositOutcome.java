package com.grahambartley.toomanychests.operation;

import java.util.Objects;

/**
 * What one deposit did: how much the player offered, and how much the network took.
 *
 * <p>What was not taken is still in the player's inventory, never on the floor, so the difference
 * between these two numbers is a report rather than a loss.
 *
 * @param result how the deposit ended
 * @param offered how much the player put forward
 * @param moved how much the network really took
 */
public record DepositOutcome(DepositResult result, long offered, long moved) {
  public DepositOutcome {
    Objects.requireNonNull(result, "result");
    if (offered < 0L) {
      throw new IllegalArgumentException("offered must not be negative: " + offered);
    }
    if (moved < 0L) {
      throw new IllegalArgumentException("moved must not be negative: " + moved);
    }
    if (moved > offered) {
      throw new IllegalArgumentException("a deposit cannot store more than it was offered");
    }
    if (result.movedAnything() != moved > 0L) {
      throw new IllegalArgumentException(result + " does not describe storing " + moved);
    }
  }

  public static DepositOutcome refused() {
    return new DepositOutcome(DepositResult.REFUSED, 0L, 0L);
  }

  public static DepositOutcome nothingToDeposit() {
    return new DepositOutcome(DepositResult.NOTHING_TO_DEPOSIT, 0L, 0L);
  }

  public static DepositOutcome noSpace(long offered) {
    return new DepositOutcome(DepositResult.NO_SPACE, offered, 0L);
  }

  /** Decides between a whole and a partial deposit from the amounts themselves. */
  public static DepositOutcome stored(long offered, long moved) {
    if (moved <= 0L) {
      throw new IllegalArgumentException("a deposit that stored nothing needs a reason");
    }
    return new DepositOutcome(
        moved >= offered ? DepositResult.DEPOSITED : DepositResult.PARTIAL, offered, moved);
  }

  /** What the player is still holding, which is the half of the report that matters to them. */
  public long remaining() {
    return offered - moved;
  }
}
