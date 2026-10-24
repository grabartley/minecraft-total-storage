package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.storage.ContainerRole;

public final class ContainerRoleCycle {
  private static final ContainerRole[] ORDER = {
    ContainerRole.GENERAL, ContainerRole.FILTERED, ContainerRole.EXCLUDED
  };

  private ContainerRoleCycle() {}

  public static ContainerRole next(ContainerRole role) {
    return ORDER[Math.floorMod(indexOf(role) + 1, ORDER.length)];
  }

  public static ContainerRole previous(ContainerRole role) {
    return ORDER[Math.floorMod(indexOf(role) - 1, ORDER.length)];
  }

  private static int indexOf(ContainerRole role) {
    final ContainerRole current = role == null ? ContainerRole.DEFAULT : role;
    for (int index = 0; index < ORDER.length; index++) {
      if (ORDER[index] == current) {
        return index;
      }
    }
    return 0;
  }
}
