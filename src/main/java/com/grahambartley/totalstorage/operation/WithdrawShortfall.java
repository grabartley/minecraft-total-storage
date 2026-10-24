package com.grahambartley.totalstorage.operation;

import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class WithdrawShortfall {
  private WithdrawShortfall() {}

  public static <V> ShortfallCause causeOf(
      List<Storage<V>> sources, V resource, long outstanding, TransactionContext transaction) {
    Objects.requireNonNull(sources, "sources");
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(transaction, "transaction");
    for (final Storage<V> source : sources) {
      if (StorageUtil.simulateExtract(source, resource, outstanding, transaction) > 0L) {
        return ShortfallCause.NO_ROOM;
      }
    }
    return ShortfallCause.NO_STOCK;
  }
}
