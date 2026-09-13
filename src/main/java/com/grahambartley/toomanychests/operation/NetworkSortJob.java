package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.sort.StorageSortResult;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.UUID;

public final class NetworkSortJob implements NetworkJob {
  private final NetworkOperationContext context;
  private final ContainerSortStep sorting;
  private final Deque<StorageOrigin> pending;
  private final OperationTally tally;

  public NetworkSortJob(NetworkOperationContext context) {
    this(context, new ContainerSortStep());
  }

  public NetworkSortJob(NetworkOperationContext context, ContainerSortStep sorting) {
    this.context = Objects.requireNonNull(context, "context");
    this.sorting = Objects.requireNonNull(sorting, "sorting");
    this.pending = new ArrayDeque<>(context.containers());
    this.tally = new OperationTally(pending.size());
  }

  @Override
  public ExpensiveOperation operation() {
    return ExpensiveOperation.SORT_NETWORK;
  }

  @Override
  public UUID actor() {
    return context.actor();
  }

  @Override
  public boolean hasNothingToDo() {
    return pending.isEmpty();
  }

  @Override
  public NetworkJobState advance(int budget) {
    if (tally.wasCancelled() || pending.isEmpty()) {
      return NetworkJobState.DONE;
    }
    if (context.network().isEmpty()) {
      cancel();
      return NetworkJobState.DONE;
    }
    int spent = 0;
    while (spent < Math.max(1, budget) && !pending.isEmpty()) {
      final StorageSortResult sorted = sorting.sortOne(context, pending.poll());
      tally.sorted(sorted);
      tally.stepped();
      spent += sorted.slotsRead();
    }
    if (!pending.isEmpty()) {
      return NetworkJobState.RUNNING;
    }
    tally.finished();
    return NetworkJobState.DONE;
  }

  @Override
  public OperationReport report() {
    return tally.report();
  }

  @Override
  public void cancel() {
    tally.cancelled();
    pending.clear();
  }
}
