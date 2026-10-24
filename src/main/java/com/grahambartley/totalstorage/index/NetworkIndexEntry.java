package com.grahambartley.totalstorage.index;

import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.List;
import java.util.Objects;

public record NetworkIndexEntry<V>(V variant, long totalCount, List<StorageOrigin> origins) {
  public NetworkIndexEntry {
    Objects.requireNonNull(variant, "variant");
    Objects.requireNonNull(origins, "origins");
    if (totalCount < 0L) {
      throw new IllegalArgumentException("totalCount must not be negative: " + totalCount);
    }
    origins = List.copyOf(origins);
  }

  public int providerCount() {
    return origins.size();
  }

  public boolean isHeldBy(StorageOrigin origin) {
    return origins.contains(origin);
  }
}
