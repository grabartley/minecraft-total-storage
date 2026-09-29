package com.grahambartley.toomanychests.operation;

import java.util.List;
import java.util.Objects;

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
    if (counted != offered - moved) {
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

  public static DepositOutcome noSpace(long offered, List<DepositLeftover> leftovers) {
    return new DepositOutcome(DepositResult.NO_SPACE, offered, 0L, leftovers);
  }

  public static DepositOutcome onlyFilteredSpace(long offered, List<DepositLeftover> leftovers) {
    return new DepositOutcome(DepositResult.ONLY_FILTERED_SPACE, offered, 0L, leftovers);
  }

  public static DepositOutcome noFittingSpace(long offered, List<DepositLeftover> leftovers) {
    return new DepositOutcome(DepositResult.NO_FITTING_SPACE, offered, 0L, leftovers);
  }

  public static DepositOutcome rateLimited() {
    return new DepositOutcome(DepositResult.RATE_LIMITED, 0L, 0L, List.of());
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

  public long remaining() {
    return offered - moved;
  }

  public int leftoverKinds() {
    return leftovers.size();
  }
}
