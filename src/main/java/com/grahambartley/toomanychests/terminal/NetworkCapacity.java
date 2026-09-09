package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerRole;

public record NetworkCapacity(int usedSlots, int totalSlots) {
  public static final NetworkCapacity EMPTY = new NetworkCapacity(0, 0);

  public static NetworkCapacity of(java.util.Collection<ContainerRoleEntry> rows) {
    int used = 0;
    int total = 0;
    for (final ContainerRoleEntry row : rows) {
      if (row.role() == ContainerRole.EXCLUDED) {
        continue;
      }
      used += row.usedSlots();
      total += row.totalSlots();
    }
    return new NetworkCapacity(used, total);
  }

  public NetworkCapacity {
    if (usedSlots < 0 || totalSlots < 0) {
      throw new IllegalArgumentException("a capacity cannot count below zero");
    }
    if (usedSlots > totalSlots) {
      throw new IllegalArgumentException(
          "a network cannot use " + usedSlots + " of " + totalSlots + " slots");
    }
  }

  public int freeSlots() {
    return totalSlots - usedSlots;
  }

  public int percentUsed() {
    if (totalSlots <= 0) {
      return 0;
    }
    return (int) Math.ceil(usedSlots * 100.0 / totalSlots);
  }

  public boolean isFull() {
    return totalSlots > 0 && freeSlots() == 0;
  }

  public boolean isUnknown() {
    return totalSlots == 0;
  }

  public boolean isNearCapacity(int warningPercent) {
    return !isUnknown() && warningPercent > 0 && percentUsed() >= warningPercent;
  }
}
