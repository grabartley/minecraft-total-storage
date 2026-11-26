package com.grahambartley.totalstorage.operation;

import com.grahambartley.totalstorage.storage.ContainerAcceptance;
import com.grahambartley.totalstorage.storage.ContainerFilter;
import com.grahambartley.totalstorage.storage.ContainerRole;
import java.util.Collection;
import java.util.Objects;
import net.minecraft.util.math.BlockPos;

public record DepositTarget(
    BlockPos container, ContainerRole role, ContainerFilter filter, ContainerContents contents) {
  public DepositTarget {
    Objects.requireNonNull(container, "container");
    container = container.toImmutable();
    role = role == null ? ContainerRole.DEFAULT : role;
    filter = filter == null ? ContainerFilter.NONE : filter;
    contents = contents == null ? ContainerContents.OTHER_ITEMS : contents;
  }

  public boolean accepts(String itemId, Collection<String> itemTagIds) {
    return ContainerAcceptance.accepts(role, filter, itemId, itemTagIds);
  }
}
