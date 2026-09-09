package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerRole;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;

public record HighlightedContainer(BlockPos container, ContainerRole role) {
  public HighlightedContainer {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    role = role == null ? ContainerRole.DEFAULT : role;
  }
}
