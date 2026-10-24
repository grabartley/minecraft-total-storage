package com.grahambartley.totalstorage.storage;

import java.util.Collection;
import java.util.Set;

public final class ContainerAcceptance {
  private ContainerAcceptance() {}

  public static boolean accepts(
      ContainerRole role, ContainerFilter filter, String itemId, Collection<String> itemTagIds) {
    final ContainerRole decided = role == null ? ContainerRole.DEFAULT : role;
    final Collection<String> tags = itemTagIds == null ? Set.of() : itemTagIds;
    return switch (decided) {
      case GENERAL -> true;
      case FILTERED -> (filter == null ? ContainerFilter.NONE : filter).accepts(itemId, tags);
      case EXCLUDED -> false;
    };
  }
}
