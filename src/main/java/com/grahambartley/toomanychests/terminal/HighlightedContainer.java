package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;

/**
 * One container the world is about to draw an outline around, and what its network may do to it.
 *
 * <p>The role travels with the position because it is the answer the highlight exists to give.
 * Seeing which chests are in range is useful; seeing which of them the mod will never write to is
 * the reassurance ADR 0004 is built around, and it cannot be read off a position alone.
 */
public record HighlightedContainer(BlockPos container, ContainerRole role) {
  public HighlightedContainer {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    role = role == null ? ContainerRole.DEFAULT : role;
  }
}
