package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.lock.LockedSlots;
import com.grahambartley.totalstorage.lock.LockedSlotsAttachment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.entity.player.PlayerEntity;

public final class PlayerSlots {

  public static final int HOTBAR_SLOTS = 9;

  public static final int FIRST_MAIN_SLOT = LockedSlots.FIRST_SLOT + HOTBAR_SLOTS;

  private PlayerSlots() {}

  public static List<SingleSlotStorage<ItemVariant>> unlocked(PlayerEntity player) {
    Objects.requireNonNull(player, "player");
    return unlocked(player, LockedSlotsAttachment.get(player));
  }

  public static List<SingleSlotStorage<ItemVariant>> unlocked(
      PlayerEntity player, LockedSlots locks) {
    Objects.requireNonNull(player, "player");
    final LockedSlots locked = locks == null ? LockedSlots.empty() : locks;
    final InventoryStorage inventory = InventoryStorage.of(player.getInventory(), null);
    final List<SingleSlotStorage<ItemVariant>> open = new ArrayList<>(LockedSlots.SLOT_COUNT);
    for (int index = LockedSlots.FIRST_SLOT; index <= LockedSlots.LAST_SLOT; index++) {
      if (!locked.isLocked(index)) {
        open.add(inventory.getSlot(index));
      }
    }
    return List.copyOf(open);
  }

  public static List<SingleSlotStorage<ItemVariant>> depositable(
      PlayerEntity player, boolean skipHotbar) {
    Objects.requireNonNull(player, "player");
    return depositable(player, LockedSlotsAttachment.get(player), skipHotbar);
  }

  public static List<SingleSlotStorage<ItemVariant>> depositable(
      PlayerEntity player, LockedSlots locks, boolean skipHotbar) {
    Objects.requireNonNull(player, "player");
    final LockedSlots locked = locks == null ? LockedSlots.empty() : locks;
    final InventoryStorage inventory = InventoryStorage.of(player.getInventory(), null);
    final int first = skipHotbar ? FIRST_MAIN_SLOT : LockedSlots.FIRST_SLOT;
    final List<SingleSlotStorage<ItemVariant>> open = new ArrayList<>(LockedSlots.SLOT_COUNT);
    for (int index = first; index <= LockedSlots.LAST_SLOT; index++) {
      if (!locked.isLocked(index)) {
        open.add(inventory.getSlot(index));
      }
    }
    return List.copyOf(open);
  }

  public static CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> unlockedStorage(
      PlayerEntity player) {
    return new CombinedStorage<>(unlocked(player));
  }

  public static SingleSlotStorage<ItemVariant> slotAt(PlayerEntity player, int index) {
    Objects.requireNonNull(player, "player");
    if (!LockedSlots.isValidSlot(index)) {
      throw new IndexOutOfBoundsException("no lockable player slot at index " + index);
    }
    return InventoryStorage.of(player.getInventory(), null).getSlot(index);
  }
}
