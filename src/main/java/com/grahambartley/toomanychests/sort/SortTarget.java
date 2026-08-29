package com.grahambartley.toomanychests.sort;

import java.util.List;
import java.util.Objects;
import net.minecraft.inventory.Inventory;

/**
 * The slots one sort is allowed to rearrange, and the inventory they belong to.
 *
 * <p>Indices are into the backing inventory rather than into the screen handler, because a handler
 * renumbers its slots per screen while the inventory does not, and the sort writes through the
 * inventory. Only the indices listed here are touched, which is how a locked player slot keeps both
 * its position and its contents.
 *
 * @param inventory the one inventory every listed index belongs to
 * @param indices the rearrangeable indices, in the order the arrangement is written back
 */
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

  /** The most one slot here can hold, which caps how large an arranged stack may be. */
  public int slotCapacity() {
    return inventory.getMaxCountPerStack();
  }
}
