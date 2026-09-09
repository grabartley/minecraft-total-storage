package com.grahambartley.toomanychests.operation;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class WithdrawShortfall {
  private WithdrawShortfall() {}

  public static <V> WithdrawOutcome of(
      Storage<V> destination, V resource, long requested, TransactionContext transaction) {
    Objects.requireNonNull(destination, "destination");
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(transaction, "transaction");
    return StorageUtil.simulateInsert(destination, resource, requested, transaction) > 0L
        ? WithdrawOutcome.nothingAvailable(requested)
        : WithdrawOutcome.noRoom(requested);
  }
}
