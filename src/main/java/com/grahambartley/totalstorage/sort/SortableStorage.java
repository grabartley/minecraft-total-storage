package com.grahambartley.totalstorage.sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.item.Items;

public final class SortableStorage {
  public static final int FULL_STACK = 64;

  private static final int MIN_SLOTS = 2;

  private static final ItemVariant FIRST_PROBE = ItemVariant.of(Items.COBBLESTONE);
  private static final ItemVariant SECOND_PROBE = ItemVariant.of(Items.DIAMOND);

  private SortableStorage() {}

  public static Optional<List<SingleSlotStorage<ItemVariant>>> slotsOf(
      Storage<ItemVariant> storage, TransactionContext transaction) {
    Objects.requireNonNull(transaction, "transaction");
    if (!(storage instanceof SlottedStorage<ItemVariant> slotted)
        || slotted.getSlotCount() < MIN_SLOTS) {
      return Optional.empty();
    }
    final List<SingleSlotStorage<ItemVariant>> slots = new ArrayList<>(slotted.getSlotCount());
    for (int index = 0; index < slotted.getSlotCount(); index++) {
      slots.add(slotted.getSlot(index));
    }
    return acceptsAnythingInAnySlot(slots, transaction)
        ? Optional.of(List.copyOf(slots))
        : Optional.empty();
  }

  private static boolean acceptsAnythingInAnySlot(
      List<SingleSlotStorage<ItemVariant>> slots, TransactionContext transaction) {
    try (Transaction probing = transaction.openNested()) {
      return emptied(slots, probing) && takesBothProbes(slots, probing);
    }
  }

  private static boolean emptied(
      List<SingleSlotStorage<ItemVariant>> slots, TransactionContext probing) {
    for (final SingleSlotStorage<ItemVariant> slot : slots) {
      final long amount = slot.getAmount();
      if (slot.isResourceBlank() || amount <= 0L) {
        continue;
      }
      if (slot.extract(slot.getResource(), amount, probing) != amount) {
        return false;
      }
    }
    return true;
  }

  private static boolean takesBothProbes(
      List<SingleSlotStorage<ItemVariant>> slots, TransactionContext probing) {
    for (final SingleSlotStorage<ItemVariant> slot : slots) {
      if (StorageUtil.simulateInsert(slot, FIRST_PROBE, FULL_STACK, probing) != FULL_STACK
          || StorageUtil.simulateInsert(slot, SECOND_PROBE, 1L, probing) != 1L) {
        return false;
      }
    }
    return true;
  }
}
