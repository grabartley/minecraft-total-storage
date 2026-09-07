package com.grahambartley.toomanychests.listing;

import java.util.Objects;

/**
 * One cell of the terminal grid: what it is, how much of it the whole network holds, and across how
 * many containers.
 *
 * <p>Which containers those are is deliberately absent. A row exists to be drawn, and per ADR 0009
 * a withdrawal names the variant it wants rather than the container it came from, so telling a
 * client where every stack lives would be handing out a map it has no use for. A count of them is
 * not that map: it answers "is this one chest or twelve" without naming a single position.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 */
public record ListingRow<V>(V variant, long count, int providerCount) {
  public ListingRow {
    Objects.requireNonNull(variant, "variant");
    if (count < 0L) {
      throw new IllegalArgumentException("count must not be negative: " + count);
    }
    if (providerCount < 0) {
      throw new IllegalArgumentException("providerCount must not be negative: " + providerCount);
    }
  }

  /** Whether naming the number of containers tells the player anything they cannot already see. */
  public boolean isSpreadAcrossContainers() {
    return providerCount > 1;
  }
}
