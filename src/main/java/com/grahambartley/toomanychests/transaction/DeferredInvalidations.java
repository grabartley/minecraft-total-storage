package com.grahambartley.toomanychests.transaction;

import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/**
 * Index bookkeeping that rolls back with the items it describes.
 *
 * <p>An operation that touches a container has to tell the index about it, but telling it while the
 * move is still provisional means a rolled back move leaves the index claiming a change that never
 * happened. Recording here instead defers every invalidation to the commit that makes it true, and
 * an abort discards them along with the move.
 *
 * <p>The snapshot is the pending count rather than a copy of the list, because recording only ever
 * appends: rolling back to a snapshot is dropping everything recorded after it.
 */
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
