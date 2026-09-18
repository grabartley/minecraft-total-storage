package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerNameRule;
import com.grahambartley.toomanychests.storage.ContainerRole;
import com.grahambartley.toomanychests.tag.NetworkTagSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.minecraft.util.math.BlockPos;

public record ContainerRoleEntry(
    BlockPos container,
    String name,
    ContainerRole role,
    List<String> acceptedTags,
    Optional<ItemVariant> icon,
    int variantCount,
    long itemCount,
    int usedSlots,
    int totalSlots,
    double distanceSq) {
  public static final int MAX_ACCEPTED_TAGS = NetworkTagSet.MAX_TOTAL;

  public ContainerRoleEntry {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    name = ContainerNameRule.sanitise(name);
    role = role == null ? ContainerRole.DEFAULT : role;
    acceptedTags = acceptedTags == null ? List.of() : List.copyOf(acceptedTags);
    icon = icon == null ? Optional.empty() : icon;
    if (acceptedTags.size() > MAX_ACCEPTED_TAGS) {
      throw new IllegalArgumentException(
          "a container row may not carry " + acceptedTags.size() + " accepted tags");
    }
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

  public boolean isNamed() {
    return !name.isEmpty();
  }

  public boolean isFiltered() {
    return role == ContainerRole.FILTERED;
  }

  public boolean acceptsNothing() {
    return isFiltered() && acceptedTags.isEmpty();
  }

  public boolean accepts(String tagId) {
    return acceptedTags.contains(tagId);
  }
}
