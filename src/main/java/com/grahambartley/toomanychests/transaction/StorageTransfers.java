package com.grahambartley.toomanychests.transaction;

import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Moving a resource between storages without ever losing or duplicating it.
 *
 * <p>Every move inserts before it extracts and abandons the pair unless the two amounts match, so a
 * destination that accepts less than it claims costs a failed move rather than the difference in
 * items. Each hop gets its own nested transaction for the same reason: one hop failing must not
 * take the hops that succeeded with it, and must not survive on its own either.
 *
 * <p>Generic over the resource type so the arithmetic is unit testable, per ADR 0012. Production
 * uses {@code ItemVariant}.
 */
public final class StorageTransfers {
  private StorageTransfers() {}

  /** Moves as much of the resource as both ends allow, up to {@code maxAmount}. */
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

  /** Moves the whole amount or nothing at all. */
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

  /**
   * Collects a resource from several containers into one, which is what a withdrawal from a network
   * is. Sources are visited in order and the whole gather commits together.
   */
  public static <V> long gather(
      List<? extends Storage<V>> sources,
      Storage<V> to,
      V resource,
      long maxAmount,
      TransactionContext transaction) {
    Objects.requireNonNull(sources, "sources");
    Objects.requireNonNull(transaction, "transaction");
    requireNotNegative(maxAmount);
    long moved = 0L;
    try (Transaction gathering = transaction.openNested()) {
      for (final Storage<V> source : sources) {
        if (moved >= maxAmount) {
          break;
        }
        moved += move(source, to, resource, maxAmount - moved, gathering);
      }
      if (moved <= 0L) {
        return 0L;
      }
      gathering.commit();
      return moved;
    }
  }

  /**
   * Spreads a resource out of one storage into several, which is what a deposit into a network is.
   * Destinations are visited in the order they are given, so the routing that ordered them is what
   * decides where an item lands rather than whichever container happens to be nearest in memory.
   */
  public static <V> long distribute(
      Storage<V> from,
      List<? extends Storage<V>> destinations,
      V resource,
      long maxAmount,
      TransactionContext transaction) {
    Objects.requireNonNull(from, "from");
    Objects.requireNonNull(destinations, "destinations");
    Objects.requireNonNull(transaction, "transaction");
    requireNotNegative(maxAmount);
    long moved = 0L;
    try (Transaction spreading = transaction.openNested()) {
      for (final Storage<V> destination : destinations) {
        if (moved >= maxAmount) {
          break;
        }
        moved += move(from, destination, resource, maxAmount - moved, spreading);
      }
      if (moved <= 0L) {
        return 0L;
      }
      spreading.commit();
      return moved;
    }
  }

  /** Collects the whole amount across containers, or leaves every one of them untouched. */
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
