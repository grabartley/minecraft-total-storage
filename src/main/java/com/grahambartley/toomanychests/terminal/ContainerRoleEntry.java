package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;

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
