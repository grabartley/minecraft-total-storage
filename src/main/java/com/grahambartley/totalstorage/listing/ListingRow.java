package com.grahambartley.totalstorage.listing;

import java.util.Objects;

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

  public boolean isSpreadAcrossContainers() {
    return providerCount > 1;
  }
}
