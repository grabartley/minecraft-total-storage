package com.grahambartley.totalstorage.transaction;

import com.grahambartley.totalstorage.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public final class DeferredInvalidations extends SnapshotParticipant<Integer> {
  private final Consumer<StorageOrigin> sink;
  private final List<StorageOrigin> pending = new ArrayList<>();

  public DeferredInvalidations(Consumer<StorageOrigin> sink) {
    this.sink = Objects.requireNonNull(sink, "sink");
  }

  public void record(StorageOrigin origin, TransactionContext transaction) {
    Objects.requireNonNull(origin, "origin");
    Objects.requireNonNull(transaction, "transaction");
    updateSnapshots(transaction);
    pending.add(origin);
  }

  public int pendingCount() {
    return pending.size();
  }

  @Override
  protected Integer createSnapshot() {
    return pending.size();
  }

  @Override
  protected void readSnapshot(Integer recordedBefore) {
    pending.subList(recordedBefore, pending.size()).clear();
  }

  @Override
  protected void onFinalCommit() {
    final List<StorageOrigin> invalidating = pending.stream().distinct().toList();
    pending.clear();
    invalidating.forEach(sink);
  }
}
