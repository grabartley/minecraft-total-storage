package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.config.ServerConfigService;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.mutation.ContainerMutationWitness;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.screenhandler.ContainerViewerSync;
import com.grahambartley.toomanychests.storage.StorageOrigin;
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
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;

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
    final OperationResult<Long> result;
    final List<StorageOrigin> changed;
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.overEverything(ContainerMutations.registry())) {
      result =
          NetworkTransactions.runner().run(transaction -> rearrange(slots, target, transaction));
      if (result.committed()) {
        target.inventory().markDirty();
      }
      changed = List.copyOf(witness.changed());
    }
    if (result.rolledBack()) {
      return SortOutcome.FAILED;
    }
    resync(player, target, changed);
    return SortOutcome.SORTED;
  }

  private static void resync(
      ServerPlayerEntity player, SortTarget target, List<StorageOrigin> changed) {
    final List<Inventory> showing =
        new ArrayList<>(ContainerViewerSync.inventoriesOf(player.getServer(), changed));
    showing.add(target.inventory());
    ContainerViewerSync.resyncAll(player.getServer(), showing);
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
