package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lock.LockedSlots;

public final class WithdrawAmount {
  private WithdrawAmount() {}

  public static int fullStack(int maxStackSize) {
    return Math.max(0, maxStackSize);
  }

  public static int halfStack(int maxStackSize) {
    return maxStackSize <= 0 ? 0 : (maxStackSize + 1) / 2;
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
