package com.grahambartley.totalstorage.transaction;

import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class StorageTransfers {
  private StorageTransfers() {}

  public static <V> long move(
      Storage<V> from, Storage<V> to, V resource, long maxAmount, TransactionContext transaction) {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(to, "to");
    Objects.requireNonNull(resource, "resource");
    Objects.requireNonNull(transaction, "transaction");
    requireNotNegative(maxAmount);
    if (maxAmount == 0L) {
      return 0L;
    }
    try (Transaction moving = transaction.openNested()) {
      final long extractable = StorageUtil.simulateExtract(from, resource, maxAmount, moving);
      if (extractable <= 0L) {
        return 0L;
      }
      final long inserted = StorageUtil.tryInsertStacking(to, resource, extractable, moving);
      if (inserted <= 0L) {
        return 0L;
      }
      if (from.extract(resource, inserted, moving) != inserted) {
        return 0L;
      }
      moving.commit();
      return inserted;
    }
  }

  public static <V> boolean moveExactly(
      Storage<V> from, Storage<V> to, V resource, long amount, TransactionContext transaction) {
    requirePositive(amount);
    Objects.requireNonNull(transaction, "transaction");
    try (Transaction moving = transaction.openNested()) {
      if (move(from, to, resource, amount, moving) != amount) {
        return false;
      }
      moving.commit();
      return true;
    }
  }

  public static <V> long gather(
      List<? extends Storage<V>> sources,
      Storage<V> to,
      V resource,
      long maxAmount,
      TransactionContext transaction) {
    Objects.requireNonNull(sources, "sources");
    Objects.requireNonNull(to, "to");
    return across(
        sources,
        maxAmount,
        transaction,
        (source, remaining, nested) -> move(source, to, resource, remaining, nested));
  }

  public static <V> long distribute(
      Storage<V> from,
      List<? extends Storage<V>> destinations,
      V resource,
      long maxAmount,
      TransactionContext transaction) {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(destinations, "destinations");
    return across(
        destinations,
        maxAmount,
        transaction,
        (destination, remaining, nested) -> move(from, destination, resource, remaining, nested));
  }

  public static <V> boolean gatherExactly(
      List<? extends Storage<V>> sources,
      Storage<V> to,
      V resource,
      long amount,
      TransactionContext transaction) {
    requirePositive(amount);
    Objects.requireNonNull(transaction, "transaction");
    try (Transaction gathering = transaction.openNested()) {
      if (gather(sources, to, resource, amount, gathering) != amount) {
        return false;
      }
      gathering.commit();
      return true;
    }
  }

  private static <V> long across(
      List<? extends Storage<V>> hops, long maxAmount, TransactionContext transaction, Hop<V> hop) {
    Objects.requireNonNull(transaction, "transaction");
    requireNotNegative(maxAmount);
    long moved = 0L;
    try (Transaction spanning = transaction.openNested()) {
      for (final Storage<V> next : hops) {
        if (moved >= maxAmount) {
          break;
        }
        moved += hop.move(next, maxAmount - moved, spanning);
      }
      if (moved <= 0L) {
        return 0L;
      }
      spanning.commit();
      return moved;
    }
  }

  @FunctionalInterface
  private interface Hop<V> {
    long move(Storage<V> other, long remaining, TransactionContext transaction);
  }

  private static void requireNotNegative(long amount) {
    if (amount < 0L) {
      throw new IllegalArgumentException("maxAmount must not be negative: " + amount);
    }
  }

  private static void requirePositive(long amount) {
    if (amount <= 0L) {
      throw new IllegalArgumentException("amount must be positive: " + amount);
    }
  }
}
