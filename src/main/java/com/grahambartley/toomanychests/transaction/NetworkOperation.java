package com.grahambartley.toomanychests.transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

@FunctionalInterface
public interface NetworkOperation<T> {
  OperationResult<T> perform(TransactionContext transaction);
}
