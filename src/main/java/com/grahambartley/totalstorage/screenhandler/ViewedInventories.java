package com.grahambartley.totalstorage.screenhandler;

import java.util.List;
import java.util.Set;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.slot.Slot;

public final class ViewedInventories {
  private ViewedInventories() {}

  public static boolean showAny(List<Slot> slots, Set<Inventory> changed) {
    if (slots == null || changed == null || changed.isEmpty()) {
      return false;
    }
    Inventory checked = null;
    for (final Slot slot : slots) {
      if (slot.inventory == checked) {
        continue;
      }
      checked = slot.inventory;
      if (changed.contains(checked) || isHalfOf(checked, changed)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isHalfOf(Inventory shown, Set<Inventory> changed) {
    if (!(shown instanceof DoubleInventory doubled)) {
      return false;
    }
    for (final Inventory inventory : changed) {
      if (doubled.isPart(inventory)) {
        return true;
      }
    }
    return false;
  }
}
