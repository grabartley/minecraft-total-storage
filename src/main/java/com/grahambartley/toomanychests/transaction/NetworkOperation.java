package com.grahambartley.toomanychests.transaction;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * One mutating thing the mod does to a network: a withdrawal, a deposit, a sort.
 *
 * <p>The transaction is handed in rather than opened, so an operation cannot forget to roll back
 * and cannot commit work its caller has not accepted.
 *
 * @param <T> what the operation produces, commonly the amount moved
 */
@FunctionalInterface
public interface NetworkOperation<T> {
  OperationResult<T> perform(TransactionContext transaction);
}
