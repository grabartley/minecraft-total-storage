package com.grahambartley.toomanychests.operation;

import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Which empty-handed ending a withdrawal that moved nothing really had.
 *
 * <p>A withdrawal comes back with nothing for two unrelated reasons, and a player can only act on
 * the difference: the network no longer holds what the listing offered, or it holds it and there is
 * nowhere to put it. Telling somebody their inventory is full when the item had simply gone sends
 * them to empty a slot that was never the problem.
 *
 * <p>The two are told apart by asking the destination rather than by re-reading the network, which
 * is one simulated insert against a storage already in hand. Re-reading the network would mean
 * asking every container the same question a second time, and would answer for the instant after
 * the move rather than the instant of it.
 *
 * <p>Generic over the resource type per ADR 0012, so the decision is unit testable without a world.
 */
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
