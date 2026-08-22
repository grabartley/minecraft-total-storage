package com.grahambartley.toomanychests.storage;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** What one discovery scan found, and what it cost. */
public record ContainerScanResult(
    List<StorageProvider> providers, int chunksVisited, int chunksSkipped, long elapsedNanos) {

  public static final long BUDGET_NANOS = Duration.ofMillis(10).toNanos();

  public ContainerScanResult {
    Objects.requireNonNull(providers, "providers");
    providers = List.copyOf(providers);
  }

  public static ContainerScanResult empty() {
    return new ContainerScanResult(List.of(), 0, 0, 0L);
  }

  public int containerCount() {
    return providers.size();
  }

  /** Chunks the scan declined to touch because they were not loaded. */
  public boolean skippedUnloadedChunks() {
    return chunksSkipped > 0;
  }

  /** A scan slow enough that a pack author should hear about it rather than just feel it. */
  public boolean exceededBudget() {
    return elapsedNanos > BUDGET_NANOS;
  }
}
