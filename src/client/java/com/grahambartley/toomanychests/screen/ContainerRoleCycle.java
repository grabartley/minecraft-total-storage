package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.storage.ContainerRole;

/**
 * The order clicking a container walks its role through.
 *
 * <p>A cycle rather than a menu because there are three roles and a menu for three options is a
 * menu nobody wants to open. Clicking goes forwards and right-clicking goes back, so a role
 * overshot by one click costs one click rather than two.
 *
 * <p>The order is deliberate: {@code GENERAL} to {@code FILTERED} to {@code EXCLUDED} runs from
 * what the network may do most with to what it may do least with, so a player clicking repeatedly
 * is always tightening rather than jumping about. Per ADR 0004 a filtered container that has
 * declared nothing accepts nothing, so stopping halfway through is safe.
 */
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
