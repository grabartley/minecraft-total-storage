package com.grahambartley.toomanychests.lock;

import net.minecraft.entity.player.PlayerEntity;

public final class LockToggleService {
  private LockToggleService() {}

  public static LockToggleOutcome toggle(PlayerEntity player, int slot) {
    if (player == null) {
      return LockToggleOutcome.REJECTED;
    }
    final LockToggleResult result = LockToggle.apply(LockedSlotsAttachment.get(player), slot);
    if (result.outcome().wasApplied()) {
      LockedSlotsAttachment.set(player, result.locks());
    }
    return result.outcome();
  }
}
