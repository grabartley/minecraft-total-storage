package com.grahambartley.toomanychests.transaction;

import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

/**
 * One slot of a container, holding named resources instead of item variants so the transfer rules
 * can be exercised without a running game. Participates in transactions exactly as the real thing
 * does, which is what makes rollback assertions meaningful.
 */
final class FakeSlot extends SnapshotParticipant<ResourceAmount<String>>
    implements SingleSlotStorage<String> {
  static final String BLANK = "";

  private final long capacity;

  private String resource = BLANK;
  private long amount;

  FakeSlot(long capacity) {
    this.capacity = capacity;
  }

  void set(String held, long heldAmount) {
    this.resource = held;
    this.amount = heldAmount;
  }

  @Override
  public long insert(String inserted, long maxAmount, TransactionContext transaction) {
    if (BLANK.equals(inserted) || maxAmount <= 0L) {
      return 0L;
    }
    if (!isResourceBlank() && !resource.equals(inserted)) {
      return 0L;
    }
    final long accepted = Math.min(maxAmount, capacity - amount);
    if (accepted <= 0L) {
      return 0L;
    }
    updateSnapshots(transaction);
    resource = inserted;
    amount += accepted;
    return accepted;
  }

  @Override
  public long extract(String extracted, long maxAmount, TransactionContext transaction) {
    if (isResourceBlank() || !resource.equals(extracted) || maxAmount <= 0L) {
      return 0L;
    }
    final long removed = Math.min(maxAmount, amount);
    updateSnapshots(transaction);
    amount -= removed;
    if (amount == 0L) {
      resource = BLANK;
    }
    return removed;
  }

  @Override
  public boolean isResourceBlank() {
    return BLANK.equals(resource);
  }

  @Override
  public String getResource() {
    return resource;
  }

  @Override
  public long getAmount() {
    return amount;
  }

  @Override
  public long getCapacity() {
    return capacity;
  }

  @Override
  protected ResourceAmount<String> createSnapshot() {
    return new ResourceAmount<>(resource, amount);
  }

  @Override
  protected void readSnapshot(ResourceAmount<String> snapshot) {
    resource = snapshot.resource();
    amount = snapshot.amount();
  }
}
