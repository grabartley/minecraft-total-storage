package com.grahambartley.toomanychests.listing;

import java.util.Objects;

/**
 * One cell of the terminal grid: what it is, and how much of it the whole network holds.
 *
 * <p>The containers holding it are deliberately absent. A row exists to be drawn, and per ADR 0009
 * a withdrawal names the variant it wants rather than the container it came from, so telling a
 * client where every stack lives would be handing out a map it has no use for.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 */
public record ListingRow<V>(V variant, long count) {
  public ListingRow {
    Objects.requireNonNull(variant, "variant");
    if (count < 0L) {
      throw new IllegalArgumentException("count must not be negative: " + count);
    }
  }
}
