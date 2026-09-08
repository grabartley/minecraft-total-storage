package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;

/**
 * One row of the container list: where a container is, what this network may do to it, and roughly
 * how much is in it.
 *
 * <p>The counts are what turn a list of coordinates into something a player can navigate. A row
 * reading "3 kinds, 168 items" is recognisable as the cobblestone chest in a way {@code (128, 64,
 * -32)} is not, and recognising the container is the whole reason the list exists.
 *
 * <p>Plain data, so the ordering and tallying over these rows can be proven without a world.
 *
 * @param container where it is
 * @param role what this network may do to it
 * @param variantCount how many distinct kinds of item it holds
 * @param itemCount how many items in total
 * @param distanceSq how far it sits from the terminal, which is what orders the list
 */
public record ContainerRoleEntry(
    BlockPos container, ContainerRole role, int variantCount, long itemCount, double distanceSq) {

  public ContainerRoleEntry {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    role = role == null ? ContainerRole.DEFAULT : role;
    if (variantCount < 0) {
      throw new IllegalArgumentException("variantCount must not be negative: " + variantCount);
    }
    if (itemCount < 0L) {
      throw new IllegalArgumentException("itemCount must not be negative: " + itemCount);
    }
    if (distanceSq < 0.0) {
      throw new IllegalArgumentException("distanceSq must not be negative: " + distanceSq);
    }
  }

  public boolean isEmpty() {
    return variantCount == 0;
  }
}
