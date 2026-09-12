package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class SortArrangement {
  private SortArrangement() {}

  public static <V> SortPlan<V> of(
      List<? extends SingleSlotStorage<V>> slots,
      int slotCapacity,
      SortCandidates<V> candidates,
      Comparator<SortCandidate> ordering) {
    Objects.requireNonNull(slots, "slots");
    Objects.requireNonNull(candidates, "candidates");
    Objects.requireNonNull(ordering, "ordering");
    final List<SortHolding<V>> holdings = new ArrayList<>(slots.size());
    for (final SingleSlotStorage<V> slot : slots) {
      final long amount = slot.getAmount();
      if (slot.isResourceBlank() || amount <= 0L) {
        continue;
      }
      final V variant = slot.getResource();
      holdings.add(
          new SortHolding<>(variant, candidates.of(variant, amount).cappedAt(slotCapacity)));
    }
    if (holdings.isEmpty()) {
      return SortPlan.nothing();
    }
    return new SortPlan<>(SortLayout.arrange(holdings, ordering), SortLayout.totalOf(holdings));
  }

  public static <V> boolean matches(List<? extends SingleSlotStorage<V>> slots, SortPlan<V> plan) {
    Objects.requireNonNull(slots, "slots");
    Objects.requireNonNull(plan, "plan");
    if (plan.size() > slots.size()) {
      return false;
    }
    for (int position = 0; position < slots.size(); position++) {
      if (!holds(
          slots.get(position), position < plan.size() ? plan.stacks().get(position) : null)) {
        return false;
      }
    }
    return true;
  }

  public static <V> OperationResult<Long> apply(
      List<? extends SingleSlotStorage<V>> slots,
      SortPlan<V> plan,
      TransactionContext transaction) {
    Objects.requireNonNull(slots, "slots");
    Objects.requireNonNull(plan, "plan");
    Objects.requireNonNull(transaction, "transaction");
    if (plan.isEmpty()) {
      return OperationResult.rolledBack("nothing was left to arrange");
    }
    if (plan.size() > slots.size()) {
      return OperationResult.rolledBack("the arrangement needs more slots than the container has");
    }
    final OperationResult<Long> emptied = empty(slots, plan.total(), transaction);
    if (emptied.rolledBack()) {
      return emptied;
    }
    return refill(slots, plan, transaction);
  }

  private static <V> OperationResult<Long> empty(
      List<? extends SingleSlotStorage<V>> slots, long expected, TransactionContext transaction) {
    long taken = 0L;
    for (final SingleSlotStorage<V> slot : slots) {
      final long amount = slot.getAmount();
      if (slot.isResourceBlank() || amount <= 0L) {
        continue;
      }
      if (slot.extract(slot.getResource(), amount, transaction) != amount) {
        return OperationResult.rolledBack("a slot would not give up its contents");
      }
      taken += amount;
    }
    return taken == expected
        ? OperationResult.committed(taken)
        : OperationResult.rolledBack("the container changed while it was being sorted");
  }

  private static <V> OperationResult<Long> refill(
      List<? extends SingleSlotStorage<V>> slots,
      SortPlan<V> plan,
      TransactionContext transaction) {
    long replaced = 0L;
    for (int position = 0; position < plan.size(); position++) {
      final SortHolding<V> stack = plan.stacks().get(position);
      final long inserted = slots.get(position).insert(stack.variant(), stack.count(), transaction);
      if (inserted != stack.count()) {
        return OperationResult.rolledBack("a slot would not take the stack arranged for it");
      }
      replaced += inserted;
    }
    return replaced == plan.total()
        ? OperationResult.committed(replaced)
        : OperationResult.rolledBack("sorting would not have conserved every item");
  }

  private static <V> boolean holds(SingleSlotStorage<V> slot, SortHolding<V> stack) {
    if (stack == null) {
      return slot.isResourceBlank() || slot.getAmount() <= 0L;
    }
    return !slot.isResourceBlank()
        && stack.variant().equals(slot.getResource())
        && slot.getAmount() == stack.count();
  }
}
