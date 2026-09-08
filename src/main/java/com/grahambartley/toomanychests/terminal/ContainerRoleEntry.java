package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;

/**
 * One row of the container list: what is in a container, where it is, and what this network may do
 * to it.
 *
 * <p>The icon is what makes a row recognisable. A player does not remember a chest as {@code (128,
 * 64, -32)}; they remember it as the cobblestone chest, and drawing the thing it mostly holds is
 * the difference between a list they can navigate and a list of coordinates. The counts say how
 * much without needing the container opened.
 *
 * <p>Plain data apart from the icon, so the ordering and tallying over these rows can be proven
 * without a world.
 *
 * @param container where it is
 * @param role what this network may do to it
 * @param icon the variant it holds most of, empty for a container holding nothing
 * @param variantCount how many distinct kinds of item it holds
 * @param itemCount how many items in total
 * @param usedSlots how many of its slots hold something, which is what capacity is counted in
 * @param totalSlots how many slots it has at all
 * @param distanceSq how far it sits from the terminal, which is what orders the list
 */
public record ContainerRoleEntry(
    BlockPos container,
    ContainerRole role,
    Optional<ItemVariant> icon,
    int variantCount,
    long itemCount,
    int usedSlots,
    int totalSlots,
    double distanceSq) {

  public ContainerRoleEntry {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    role = role == null ? ContainerRole.DEFAULT : role;
    icon = icon == null ? Optional.empty() : icon;
    if (variantCount < 0) {
      throw new IllegalArgumentException("variantCount must not be negative: " + variantCount);
    }
    if (itemCount < 0L) {
      throw new IllegalArgumentException("itemCount must not be negative: " + itemCount);
    }
    if (usedSlots < 0 || totalSlots < 0) {
      throw new IllegalArgumentException("a container cannot have a negative number of slots");
    }
    if (usedSlots > totalSlots) {
      throw new IllegalArgumentException(
          "a container cannot use " + usedSlots + " of " + totalSlots + " slots");
    }
    if (distanceSq < 0.0) {
      throw new IllegalArgumentException("distanceSq must not be negative: " + distanceSq);
    }
  }

  public boolean isEmpty() {
    return variantCount == 0;
  }
}
