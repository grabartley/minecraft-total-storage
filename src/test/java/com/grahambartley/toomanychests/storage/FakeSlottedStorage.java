package com.grahambartley.toomanychests.storage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

final class FakeSlottedStorage implements SlottedStorage<ItemVariant> {
  private final List<SingleSlotStorage<ItemVariant>> slots;

  FakeSlottedStorage(int slotCount) {
    this.slots =
        IntStream.range(0, slotCount)
            .<SingleSlotStorage<ItemVariant>>mapToObj(i -> new FakeSingleSlotStorage())
            .toList();
  }

  @Override
  public int getSlotCount() {
    return slots.size();
  }

  @Override
  public SingleSlotStorage<ItemVariant> getSlot(int slot) {
    return slots.get(slot);
  }

  @Override
  public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
    return 0L;
  }

  @Override
  public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
    return 0L;
  }

  @Override
  public Iterator<StorageView<ItemVariant>> iterator() {
    return new ArrayList<StorageView<ItemVariant>>(slots).iterator();
  }
}
