package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.screenhandler.ContainerViewerSync;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Sorts whatever container a player currently has open, and is the only path a client request may
 * take.
 *
 * <p>Per ADR 0009 the request names a screen and nothing else. The sync id is checked against the
 * handler the server already holds for that player, so a client naming someone else's chest, a
 * chest it closed a tick ago, or a position it invented gets nowhere: the server never resolves a
 * container the player is not already standing in front of.
 *
 * <p>Everything moves inside one transaction per ADR 0015. A sort empties every slot before it
 * refills them, which is the one moment a container's contents exist only in a list, so a failure
 * part way through has to put back exactly what it took. The conservation check is what makes that
 * a guarantee rather than a hope.
 */
public final class ContainerSortService {
  private final SortCandidateFactory candidates;

  public ContainerSortService() {
    this(new SortCandidateFactory());
  }

  public ContainerSortService(SortCandidateFactory candidates) {
    this.candidates = Objects.requireNonNull(candidates, "candidates");
  }

  public SortOutcome sort(ServerPlayerEntity player, int syncId) {
    if (player == null) {
      return SortOutcome.NO_OPEN_SCREEN;
    }
    final ScreenHandler handler = player.currentScreenHandler;
    if (handler == null || handler.syncId != syncId) {
      return SortOutcome.NO_OPEN_SCREEN;
    }
    final Optional<SortTarget> target =
        SortableSlots.resolve(handler, player, LockedSlotsAttachment.get(player));
    if (target.isEmpty()) {
      return SortOutcome.SCREEN_NOT_SORTABLE;
    }
    return apply(player, target.get());
  }

  private SortOutcome apply(ServerPlayerEntity player, SortTarget target) {
    final List<SingleSlotStorage<ItemVariant>> slots = slotsOf(target);
    if (slots.stream().allMatch(SingleSlotStorage::isResourceBlank)) {
      return SortOutcome.NOTHING_TO_SORT;
    }
    final OperationResult<Long> result =
        NetworkTransactions.runner().run(transaction -> rearrange(slots, target, transaction));
    if (result.rolledBack()) {
      return SortOutcome.FAILED;
    }
    target.inventory().markDirty();
    ContainerViewerSync.resync(player.getServer(), target.inventory());
    return SortOutcome.SORTED;
  }

  private static List<SingleSlotStorage<ItemVariant>> slotsOf(SortTarget target) {
    final InventoryStorage storage = InventoryStorage.of(target.inventory(), null);
    final List<SingleSlotStorage<ItemVariant>> slots = new ArrayList<>(target.slotCount());
    for (final int index : target.indices()) {
      slots.add(storage.getSlot(index));
    }
    return slots;
  }

  private OperationResult<Long> rearrange(
      List<SingleSlotStorage<ItemVariant>> slots,
      SortTarget target,
      TransactionContext transaction) {
    final List<SortHolding<ItemVariant>> emptied = new ArrayList<>(slots.size());
    for (final SingleSlotStorage<ItemVariant> slot : slots) {
      final long amount = slot.getAmount();
      if (slot.isResourceBlank() || amount <= 0L) {
        continue;
      }
      final ItemVariant variant = slot.getResource();
      if (slot.extract(variant, amount, transaction) != amount) {
        return OperationResult.rolledBack("a slot would not give up its contents");
      }
      emptied.add(
          new SortHolding<>(
              variant, candidates.of(variant, amount).cappedAt(target.slotCapacity())));
    }
    if (emptied.isEmpty()) {
      return OperationResult.rolledBack("nothing was left to arrange");
    }
    return refill(
        slots, SortLayout.arrange(emptied, ordering()), SortLayout.totalOf(emptied), transaction);
  }

  private static OperationResult<Long> refill(
      List<SingleSlotStorage<ItemVariant>> slots,
      List<SortHolding<ItemVariant>> arranged,
      long expected,
      TransactionContext transaction) {
    if (arranged.size() > slots.size()) {
      return OperationResult.rolledBack("the arrangement needs more slots than the container has");
    }
    long replaced = 0L;
    for (int position = 0; position < arranged.size(); position++) {
      final SortHolding<ItemVariant> stack = arranged.get(position);
      final long inserted = slots.get(position).insert(stack.variant(), stack.count(), transaction);
      if (inserted != stack.count()) {
        return OperationResult.rolledBack("a slot would not take the stack arranged for it");
      }
      replaced += inserted;
    }
    return replaced == expected
        ? OperationResult.committed(replaced)
        : OperationResult.rolledBack("sorting would not have conserved every item");
  }

  private static Comparator<SortCandidate> ordering() {
    return SortComparator.forOrdering(ServerConfigService.current().sortOrdering());
  }
}
