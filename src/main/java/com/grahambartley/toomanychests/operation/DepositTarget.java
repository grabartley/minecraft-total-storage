package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.storage.ContainerFilter;
import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;

/**
 * One container a deposit could send an item to, flattened to the facts routing decides on.
 *
 * <p>Plain data rather than a live storage, so the routing order is a pure function that can be
 * exhausted without a world. Whether the container has room is deliberately not among these facts:
 * a full container accepts nothing and the next one in the order is tried, which is the same
 * outcome as never offering it and one fewer thing to keep true.
 *
 * @param container where the container is, which is what breaks ties
 * @param role what this network is allowed to do to it
 * @param filter what it accepts, meaningful only for a {@code FILTERED} container
 * @param holdsItem whether it already holds the item being deposited, so like items stay together
 */
public record DepositTarget(
    BlockPos container, ContainerRole role, ContainerFilter filter, boolean holdsItem) {

  public DepositTarget {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    role = role == null ? ContainerRole.DEFAULT : role;
    filter = filter == null ? ContainerFilter.NONE : filter;
  }

  /**
   * Whether this container will take the item at all. A {@code FILTERED} container that has not
   * declared the item is passed over rather than used as a fallback, which is the promise its role
   * makes; an {@code EXCLUDED} container is never written to, per ADR 0004.
   */
  public boolean accepts(String itemId, Collection<String> itemTagIds) {
    return switch (role) {
      case GENERAL -> true;
      case FILTERED -> filter.accepts(itemId, itemTagIds);
      case EXCLUDED -> false;
    };
  }
}
