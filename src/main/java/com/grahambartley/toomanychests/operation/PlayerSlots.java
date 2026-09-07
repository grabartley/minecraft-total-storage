package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lock.LockedSlots;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.minecraft.entity.player.PlayerEntity;

/**
 * The slots of a player's own inventory an automated operation is allowed to use.
 *
 * <p>Locked slots are absent rather than skipped later, so every path built on this inherits ADR
 * 0010 by construction instead of by remembering to check. A path that could see a locked slot at
 * all is a path that could forget, and per the locking epic a lock one operation ignores is worse
 * than no lock.
 *
 * <p>Armour and the offhand fall out for free: they sit above the lockable range, which is exactly
 * the range a deposit and a shift-click withdrawal are allowed to touch.
 *
 * <p>Slots are listed hotbar first, matching {@code PlayerInventory}'s own numbering, so items land
 * where vanilla would have put them.
 */
public final class PlayerSlots {
  private PlayerSlots() {}

  /** The unlocked main inventory and hotbar slots, in inventory order. */
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

  /** The same slots as one storage, which is what a withdrawal inserts into. */
  public static CombinedStorage<ItemVariant, SingleSlotStorage<ItemVariant>> unlockedStorage(
      PlayerEntity player) {
    return new CombinedStorage<>(unlocked(player));
  }

  /**
   * One slot of the player's own inventory by its index.
   *
   * <p>Whether that slot may be used is the caller's decision, because only the caller knows what
   * to say when it may not: a locked slot and an index no slot has are refused for different
   * reasons and a player told the wrong one learns to distrust their locks. An index outside the
   * range is a contract breach rather than a case to handle, since every caller has already decided
   * what to do about one.
   */
  public static SingleSlotStorage<ItemVariant> slotAt(PlayerEntity player, int index) {
    Objects.requireNonNull(player, "player");
    if (!LockedSlots.isValidSlot(index)) {
      throw new IndexOutOfBoundsException("no lockable player slot at index " + index);
    }
    return InventoryStorage.of(player.getInventory(), null).getSlot(index);
  }
}
