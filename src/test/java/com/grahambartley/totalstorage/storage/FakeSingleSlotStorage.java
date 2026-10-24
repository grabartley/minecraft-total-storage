package com.grahambartley.totalstorage.storage;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

final class FakeSingleSlotStorage implements SingleSlotStorage<ItemVariant> {
  @Override
  public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
    return 0L;
  }

  @Override
  public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
    return 0L;
  }

  @Override
  public boolean isResourceBlank() {
    return true;
  }

  @Override
  public ItemVariant getResource() {
    return null;
  }

  @Override
  public long getAmount() {
    return 0L;
  }

  @Override
  public long getCapacity() {
    return 64L;
  }
}
