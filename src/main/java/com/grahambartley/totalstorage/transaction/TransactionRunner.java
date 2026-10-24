package com.grahambartley.totalstorage.transaction;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransactionRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRunner.class);

  private final BooleanSupplier onServerThread;

  public TransactionRunner(BooleanSupplier onServerThread) {
    this.onServerThread = Objects.requireNonNull(onServerThread, "onServerThread");
  }

  public <T> OperationResult<T> run(NetworkOperation<T> operation) {
    Objects.requireNonNull(operation, "operation");
    requireServerThread();
    if (Transaction.isOpen()) {
      throw new IllegalStateException(
          "A transaction is already open on this thread, use runNested to join it");
    }
    try (Transaction outer = Transaction.openOuter()) {
      return settle(operation, outer);
    }
  }

  public <T> OperationResult<T> runNested(
      TransactionContext parent, NetworkOperation<T> operation) {
    Objects.requireNonNull(parent, "parent");
    Objects.requireNonNull(operation, "operation");
    requireServerThread();
    try (Transaction nested = parent.openNested()) {
      return settle(operation, nested);
    }
  }

  private <T> OperationResult<T> settle(NetworkOperation<T> operation, Transaction transaction) {
    final OperationResult<T> result;
    try {
      result = operation.perform(transaction);
    } catch (RuntimeException failure) {
      LOGGER.error("Rolling back a network operation that threw", failure);
      return OperationResult.rolledBack("operation threw " + failure.getClass().getSimpleName());
    }
    if (result == null) {
      LOGGER.error("Rolling back a network operation that returned no result");
      return OperationResult.rolledBack("operation returned no result");
    }
    if (result.committed()) {
      transaction.commit();
    }
    return result;
  }

  private void requireServerThread() {
    if (!onServerThread.getAsBoolean()) {
      throw new IllegalStateException(
          "Network operations run on the server thread only, not "
              + Thread.currentThread().getName());
    }
  }
}
