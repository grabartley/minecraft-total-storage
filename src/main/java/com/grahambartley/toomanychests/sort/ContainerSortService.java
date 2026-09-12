package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.mutation.ContainerMutationWitness;
import com.grahambartley.toomanychests.mutation.ContainerMutations;
import com.grahambartley.toomanychests.screenhandler.ContainerViewerSync;
import com.grahambartley.toomanychests.storage.StorageOrigin;
import com.grahambartley.toomanychests.transaction.NetworkTransactions;
import com.grahambartley.toomanychests.transaction.OperationResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
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
    final SortPlan<ItemVariant> plan =
        SortArrangement.of(slots, target.slotCapacity(), candidates, ConfiguredSortOrder.current());
    if (plan.isEmpty()) {
      return SortOutcome.NOTHING_TO_SORT;
    }
    if (SortArrangement.matches(slots, plan)) {
      return SortOutcome.ALREADY_SORTED;
    }
    final OperationResult<Long> result;
    final List<StorageOrigin> changed;
    try (ContainerMutationWitness witness =
        ContainerMutationWitness.overEverything(ContainerMutations.registry())) {
      result =
          NetworkTransactions.runner()
              .run(transaction -> SortArrangement.apply(slots, plan, transaction));
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
}
