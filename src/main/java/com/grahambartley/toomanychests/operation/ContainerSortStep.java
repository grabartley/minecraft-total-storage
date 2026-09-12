package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.sort.ConfiguredSortOrder;
import com.grahambartley.toomanychests.sort.StorageSort;
import com.grahambartley.toomanychests.sort.StorageSortOutcome;
import com.grahambartley.toomanychests.sort.StorageSortResult;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.storage.StorageProvider;
import com.grahambartley.toomanychests.storage.StorageProviderResolver;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.List;
import java.util.Objects;

public final class ContainerSortStep {
  private final StorageSort sorter;
  private final StorageProviderResolver providers;

  public ContainerSortStep() {
    this(new StorageSort(), new StorageProviderResolver());
  }

  public ContainerSortStep(StorageSort sorter, StorageProviderResolver providers) {
    this.sorter = Objects.requireNonNull(sorter, "sorter");
    this.providers = Objects.requireNonNull(providers, "providers");
  }

  public StorageSortResult sortOne(NetworkOperationContext context, StorageOrigin origin) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(origin, "origin");
    final ContainerRole role = context.roleFor(origin);
    if (role == ContainerRole.EXCLUDED) {
      return StorageSortResult.of(StorageSortOutcome.EXCLUDED);
    }
    final StorageProvider provider = providers.resolve(context.world(), origin, role).orElse(null);
    if (provider == null) {
      return StorageSortResult.of(StorageSortOutcome.GONE);
    }
    return NetworkStep.run(
            context.server(),
            List.of(origin),
            context.actor(),
            transaction ->
                settle(sorter.sort(provider, ConfiguredSortOrder.current(), transaction)))
        .orElse(StorageSortResult.of(StorageSortOutcome.FAILED));
  }

  private static OperationResult<StorageSortResult> settle(StorageSortResult sorted) {
    return sorted.outcome() == StorageSortOutcome.FAILED
        ? OperationResult.rolledBack("a container would not sort")
        : OperationResult.committed(sorted);
  }
}
