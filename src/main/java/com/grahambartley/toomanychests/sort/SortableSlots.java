package com.grahambartley.toomanychests.sort;

import com.grahambartley.toomanychests.lock.LockedSlots;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public final class SortableSlots {
  private static final int MIN_SLOTS = 2;
  private static final int FULL_STACK = 64;

  private SortableSlots() {}

  public static Optional<SortTarget> resolve(
      ScreenHandler handler, PlayerEntity player, LockedSlots locks) {
    if (handler == null || player == null) {
      return Optional.empty();
    }
    if (handler instanceof PlayerScreenHandler) {
      return playerInventory(player, locks == null ? LockedSlots.empty() : locks);
    }
    return ScreenSortPolicy.allows(ScreenSortPolicy.typeIdOf(handler))
        ? container(handler, player)
        : Optional.empty();
  }

  private static Optional<SortTarget> playerInventory(PlayerEntity player, LockedSlots locks) {
    final PlayerInventory inventory = player.getInventory();
    final List<Integer> indices = new ArrayList<>(LockedSlots.SLOT_COUNT);
    for (int index = LockedSlots.FIRST_SLOT; index <= LockedSlots.LAST_SLOT; index++) {
      if (!locks.isLocked(index)) {
        indices.add(index);
      }
    }
    return indices.size() < MIN_SLOTS
        ? Optional.empty()
        : Optional.of(new SortTarget(inventory, indices));
  }

  private static Optional<SortTarget> container(ScreenHandler handler, PlayerEntity player) {
    Inventory backing = null;
    final List<Integer> indices = new ArrayList<>();
    for (final Slot slot : handler.slots) {
      if (slot.inventory instanceof PlayerInventory) {
        continue;
      }
      if (!isRearrangeable(slot, player)) {
        return Optional.empty();
      }
      if (backing == null) {
        backing = slot.inventory;
      } else if (backing != slot.inventory) {
        return Optional.empty();
      }
      indices.add(slot.getIndex());
    }
    return backing == null || indices.size() < MIN_SLOTS
        ? Optional.empty()
        : Optional.of(new SortTarget(backing, indices));
  }

  private static boolean isRearrangeable(Slot slot, PlayerEntity player) {
    return slot.getMaxItemCount() >= FULL_STACK
        && slot.canTakeItems(player)
        && slot.canInsert(new ItemStack(Items.COBBLESTONE))
        && slot.canInsert(new ItemStack(Items.DIAMOND));
  }
}
