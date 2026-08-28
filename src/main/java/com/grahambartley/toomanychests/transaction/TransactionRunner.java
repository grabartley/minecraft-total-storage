package com.grahambartley.toomanychests.transaction;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The one place in the mod that opens a transaction.
 *
 * <p>Per ADR 0015 no feature opens its own, so every mutating path gets the same guarantees for
 * free: a result that is not committed rolls back, and so does an operation that throws. The worst
 * failure a storage mod has is duplicating items, and the second worst is destroying them; both
 * come from a half-applied move surviving.
 *
 * <p>The Transfer API's transaction stack is thread local, so an operation run off the server
 * thread would silently open a second, unrelated stack while mutating server state. That is a bug
 * that surfaces as corruption much later, so it is refused here instead.
 */
public final class TransactionRunner {
  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRunner.class);

  private final BooleanSupplier onServerThread;

  public TransactionRunner(BooleanSupplier onServerThread) {
    this.onServerThread = Objects.requireNonNull(onServerThread, "onServerThread");
  }

  /** Runs an operation in its own outer transaction, committing only if it says to. */
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

  /**
   * Runs one step of a compound operation, so a step that fails rolls back alone and leaves its
   * parent free to carry on or abandon everything.
   */
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
