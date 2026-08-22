package com.grahambartley.toomanychests.index;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.List;
import java.util.Objects;

/**
 * One row of the network index: a variant, how much of it the whole network holds, and where.
 *
 * <p>The origins are carried because withdrawal has to know which containers to visit. They are a
 * hint rather than a promise, since anything acting on them revalidates before extracting.
 *
 * @param <V> the variant type, {@code ItemVariant} in production
 */
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
