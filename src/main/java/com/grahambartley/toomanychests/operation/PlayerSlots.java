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
  /**
   * How many slots at the front of the inventory are the hotbar.
   *
   * <p>{@code PlayerInventory} numbers the hotbar first, so these are the slots an operator may
   * choose to keep a deposit-all out of: they are where a player leaves the tools they are working
   * with, and emptying them is the one part of the gesture that can be unwelcome.
   */
  public static final int HOTBAR_SLOTS = 9;

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

  /**
   * The slots a deposit-all may empty, which is the unlocked ones minus the hotbar when the server
   * has asked for that.
   *
   * <p>Off by default, because locking already protects a chosen slot precisely and a blanket rule
   * over nine of them is the blunter tool. It exists because a server full of players who never
   * learn about locking would rather have the blunt one than lose their pickaxe.
   */
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
    final int first = skipHotbar ? LockedSlots.FIRST_SLOT + HOTBAR_SLOTS : LockedSlots.FIRST_SLOT;
    final List<SingleSlotStorage<ItemVariant>> open = new ArrayList<>(LockedSlots.SLOT_COUNT);
    for (int index = first; index <= LockedSlots.LAST_SLOT; index++) {
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
