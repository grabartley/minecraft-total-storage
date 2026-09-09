package com.grahambartley.toomanychests.lock;

public final class LockToggle {
  private LockToggle() {}

  public static LockToggleResult apply(LockedSlots current, int slot) {
    final LockedSlots safeCurrent = current == null ? LockedSlots.empty() : current;
    if (!LockedSlots.isValidSlot(slot)) {
      return new LockToggleResult(LockToggleOutcome.REJECTED, safeCurrent);
    }
    final LockedSlots next = safeCurrent.toggled(slot);
    return new LockToggleResult(
        next.isLocked(slot) ? LockToggleOutcome.LOCKED : LockToggleOutcome.UNLOCKED, next);
  }
}
