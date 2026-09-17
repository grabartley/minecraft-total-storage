package com.grahambartley.toomanychests.operation;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class WithdrawShortfall {
  private WithdrawShortfall() {}

  public static <V> ShortfallCause causeOf(
      Storage<V> destination, V resource, long outstanding, TransactionContext transaction) {
    Objects.requireNonNull(destination, "destination");
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(transaction, "transaction");
    return StorageUtil.simulateInsert(destination, resource, outstanding, transaction) > 0L
        ? ShortfallCause.NO_STOCK
        : ShortfallCause.NO_ROOM;
  }
}
