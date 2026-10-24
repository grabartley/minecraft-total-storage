package com.grahambartley.totalstorage.sort;

import java.util.List;
import java.util.Objects;
import net.minecraft.inventory.Inventory;

public record SortTarget(Inventory inventory, List<Integer> indices) {
  public SortTarget {
    Objects.requireNonNull(inventory, "inventory");
    Objects.requireNonNull(indices, "indices");
    indices = List.copyOf(indices);
    if (indices.size() < 2) {
      throw new IllegalArgumentException("a sort needs at least two slots: " + indices.size());
    }
  }

  public int slotCount() {
    return indices.size();
  }

  public int slotCapacity() {
    return inventory.getMaxCountPerStack();
  }
}
