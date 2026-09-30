package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lock.LockedSlots;

public final class WithdrawAmount {
  private WithdrawAmount() {}

  public static int stackFrom(int maxStackSize, long held) {
    if (maxStackSize <= 0 || held <= 0L) {
      return 0;
    }
    return (int) Math.min(maxStackSize, held);
  }

  public static int halfStackFrom(int maxStackSize, long held) {
    return (stackFrom(maxStackSize, held) + 1) / 2;
  }

  public static long ceilingFor(int maxStackSize, WithdrawDestination destination) {
    if (maxStackSize <= 0 || destination == null) {
      return 0L;
    }
    return destination.holdsOneStackOnly()
        ? maxStackSize
        : (long) maxStackSize * LockedSlots.SLOT_COUNT;
  }

  public static long resolve(long requested, int maxStackSize, WithdrawDestination destination) {
    return requested <= 0L ? 0L : Math.min(requested, ceilingFor(maxStackSize, destination));
  }
}
