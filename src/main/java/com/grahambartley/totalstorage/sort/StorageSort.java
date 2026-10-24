package com.grahambartley.totalstorage.sort;

import com.grahambartley.totalstorage.storage.ContainerRole;
import com.grahambartley.totalstorage.storage.StorageProvider;
import com.grahambartley.totalstorage.transaction.NetworkTransactions;
import com.grahambartley.totalstorage.transaction.OperationResult;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class StorageSort {
  private final SortCandidateFactory candidates;

  public StorageSort() {
    this(new SortCandidateFactory());
  }

  public StorageSort(SortCandidateFactory candidates) {
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public StorageSortResult sort(
      StorageProvider provider,
      Comparator<SortCandidate> ordering,
      TransactionContext transaction) {
    Objects.requireNonNull(provider, "provider");
    Objects.requireNonNull(ordering, "ordering");
    Objects.requireNonNull(transaction, "transaction");
    if (provider.role() == ContainerRole.EXCLUDED) {
      return StorageSortResult.of(StorageSortOutcome.EXCLUDED);
    }
    final List<SingleSlotStorage<ItemVariant>> slots =
        SortableStorage.slotsOf(provider.storage(), transaction).orElse(null);
    if (slots == null) {
      return StorageSortResult.of(StorageSortOutcome.NOT_SORTABLE);
    }
    final SortPlan<ItemVariant> plan =
        SortArrangement.of(slots, SortableStorage.FULL_STACK, candidates, ordering);
    if (plan.isEmpty()) {
      return StorageSortResult.of(StorageSortOutcome.EMPTY);
    }
    final int before = occupiedSlots(slots);
    if (SortArrangement.matches(slots, plan)) {
      return new StorageSortResult(StorageSortOutcome.ALREADY_SORTED, before, before);
    }
    final OperationResult<Long> applied =
        NetworkTransactions.runner()
            .runNested(transaction, nested -> SortArrangement.apply(slots, plan, nested));
    return applied.rolledBack()
        ? StorageSortResult.of(StorageSortOutcome.FAILED)
        : new StorageSortResult(StorageSortOutcome.SORTED, before, plan.size());
  }

  private static int occupiedSlots(List<SingleSlotStorage<ItemVariant>> slots) {
    int occupied = 0;
    for (final SingleSlotStorage<ItemVariant> slot : slots) {
      if (!slot.isResourceBlank() && slot.getAmount() > 0L) {
        occupied++;
      }
    }
    return occupied;
  }
}
