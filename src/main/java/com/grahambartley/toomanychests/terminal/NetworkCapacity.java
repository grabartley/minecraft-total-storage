package com.grahambartley.toomanychests.terminal;

import com.grahambartley.toomanychests.storage.ContainerRole;

/**
 * How much room a network has left, counted in slots across every container it may write to.
 *
 * <p>Slots rather than items, because slots are what runs out. A network holding one of each of a
 * thousand items is full while holding almost nothing, and a player told they have room by weight
 * would find that out on a failed deposit instead of on the panel.
 *
 * <p>{@code EXCLUDED} containers are counted in neither total, since a network that may not write
 * to a chest has no more room for having one nearby. Counting them would make the readout drift
 * further from the truth with every chest a player deliberately protected.
 *
 * @param usedSlots slots holding something, across writable containers
 * @param totalSlots slots those containers have at all
 */
public record NetworkCapacity(int usedSlots, int totalSlots) {
  public static final NetworkCapacity EMPTY = new NetworkCapacity(0, 0);

  /**
   * The room across a network's rows, which the settings panel has already built.
   *
   * <p>Derived from those rows rather than by walking the containers again. The walk is the
   * expensive half, it has just happened, and doing it twice for one panel would make the readout
   * cost more than everything else the panel shows put together.
   */
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

  /**
   * How full the network is, rounded up, so a network with a single free slot never reads as one
   * hundred percent full. The one case the number exists to warn about is the one it must not
   * announce early.
   */
  public int percentUsed() {
    if (totalSlots <= 0) {
      return 0;
    }
    return (int) Math.ceil(usedSlots * 100.0 / totalSlots);
  }

  /** A network with nowhere left to put a new kind of item, which is what a deposit needs. */
  public boolean isFull() {
    return totalSlots > 0 && freeSlots() == 0;
  }

  /** A network with no writable containers at all, which is not the same as one that is full. */
  public boolean isUnknown() {
    return totalSlots == 0;
  }

  /**
   * Whether the network is close enough to full to be worth warning about, at the operator's
   * threshold. A threshold of a hundred warns only at full, and a network nobody can write to is
   * never warned about because there is nothing a player could do with the warning.
   */
  public boolean isNearCapacity(int warningPercent) {
    return !isUnknown() && warningPercent > 0 && percentUsed() >= warningPercent;
  }
}
