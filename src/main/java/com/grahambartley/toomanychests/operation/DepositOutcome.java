package com.grahambartley.toomanychests.operation;

import java.util.List;
import java.util.Objects;

/**
 * What one deposit did: how much the player offered, and how much the network took.
 *
 * <p>What was not taken is still in the player's inventory, never on the floor, so the difference
 * between these two numbers is a report rather than a loss.
 *
 * <p>What stayed behind is listed by item as well as counted, because "sixty left over" and "sixty
 * cobblestone left over" answer different questions and only the second one is actionable.
 *
 * @param result how the deposit ended
 * @param offered how much the player put forward
 * @param moved how much the network really took
 * @param leftovers what stayed in the player's inventory, by item, largest first
 */
public record DepositOutcome(
    DepositResult result, long offered, long moved, List<DepositLeftover> leftovers) {
  public DepositOutcome {
    Objects.requireNonNull(result, "result");
    leftovers = leftovers == null ? List.of() : List.copyOf(leftovers);
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
    final long counted = leftovers.stream().mapToLong(DepositLeftover::amount).sum();
    if (counted > 0L && counted != offered - moved) {
      throw new IllegalArgumentException(
          "the leftovers add up to " + counted + " but " + (offered - moved) + " stayed behind");
    }
  }

  public static DepositOutcome refused() {
    return new DepositOutcome(DepositResult.REFUSED, 0L, 0L, List.of());
  }

  public static DepositOutcome nothingToDeposit() {
    return new DepositOutcome(DepositResult.NOTHING_TO_DEPOSIT, 0L, 0L, List.of());
  }

  public static DepositOutcome slotLocked() {
    return new DepositOutcome(DepositResult.SLOT_LOCKED, 0L, 0L, List.of());
  }

  public static DepositOutcome noSpace(long offered) {
    return noSpace(offered, List.of());
  }

  public static DepositOutcome noSpace(long offered, List<DepositLeftover> leftovers) {
    return new DepositOutcome(DepositResult.NO_SPACE, offered, 0L, leftovers);
  }

  /** A player asking faster than the server will answer, which changed nothing. */
  public static DepositOutcome rateLimited() {
    return new DepositOutcome(DepositResult.RATE_LIMITED, 0L, 0L, List.of());
  }

  /** Decides between a whole and a partial deposit from the amounts themselves. */
  public static DepositOutcome stored(long offered, long moved) {
    return stored(offered, moved, List.of());
  }

  public static DepositOutcome stored(long offered, long moved, List<DepositLeftover> leftovers) {
    if (moved <= 0L) {
      throw new IllegalArgumentException("a deposit that stored nothing needs a reason");
    }
    return new DepositOutcome(
        moved >= offered ? DepositResult.DEPOSITED : DepositResult.PARTIAL,
        offered,
        moved,
        leftovers);
  }

  /** What the player is still holding, which is the half of the report that matters to them. */
  public long remaining() {
    return offered - moved;
  }

  /** How many kinds of item stayed behind, which decides whether they can all be named. */
  public int leftoverKinds() {
    return leftovers.size();
  }
}
