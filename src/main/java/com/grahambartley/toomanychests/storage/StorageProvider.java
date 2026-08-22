package com.grahambartley.toomanychests.storage;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;

/**
 * Somewhere items live, independent of what kind of block or item holds them.
 *
 * <p>Cheap and short-lived. The wrapped storage must not be held across ticks without being
 * re-resolved, because whatever backs it may be gone.
 */
public record StorageProvider(
    StorageOrigin origin, Storage<ItemVariant> storage, ContainerRole role) {
  public StorageProvider {
    Objects.requireNonNull(origin, "origin");
    Objects.requireNonNull(storage, "storage");
    Objects.requireNonNull(role, "role");
  }

  public static StorageProvider of(StorageOrigin origin, Storage<ItemVariant> storage) {
    return new StorageProvider(origin, storage, ContainerRole.DEFAULT);
  }

  public boolean isSlotted() {
    return storage instanceof SlottedStorage;
  }

  /** Empty when the backing storage exposes no slots, which third-party storages need not do. */
  public OptionalInt slotCount() {
    return storage instanceof SlottedStorage<ItemVariant> slotted
        ? OptionalInt.of(slotted.getSlotCount())
        : OptionalInt.empty();
  }

  /** Empty when the backing storage exposes no slots, or the index is out of bounds. */
  public Optional<SingleSlotStorage<ItemVariant>> slot(int index) {
    if (!(storage instanceof SlottedStorage<ItemVariant> slotted)) {
      return Optional.empty();
    }
    if (index < 0 || index >= slotted.getSlotCount()) {
      return Optional.empty();
    }
    return Optional.of(slotted.getSlot(index));
  }

  public StorageProvider withRole(ContainerRole newRole) {
    return role == newRole ? this : new StorageProvider(origin, storage, newRole);
  }
}
