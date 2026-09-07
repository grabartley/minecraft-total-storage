package com.grahambartley.toomanychests.operation;

import com.grahambartley.toomanychests.lock.LockedSlots;

/**
 * How much a withdrawal may take, decided before any container is touched.
 *
 * <p>Per ADR 0009 the amount arrives from a client and is a wish rather than an instruction, so
 * every request is bounded here against what its destination could physically hold. Doing it before
 * extraction rather than after is what stops a forged request asking for a billion items and
 * turning one click into a walk of the whole network.
 *
 * <p>The full and half stack sizes live here too, so the client's gestures and the server's ceiling
 * are the same arithmetic rather than two copies of it.
 */
public final class WithdrawAmount {
  private WithdrawAmount() {}

  public static int fullStack(int maxStackSize) {
    return Math.max(0, maxStackSize);
  }

  /** Half a stack, rounded up, which is what vanilla hands over for a right click. */
  public static int halfStack(int maxStackSize) {
    return maxStackSize <= 0 ? 0 : (maxStackSize + 1) / 2;
  }

  /** The most this destination could hold of an item that stacks to {@code maxStackSize}. */
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
