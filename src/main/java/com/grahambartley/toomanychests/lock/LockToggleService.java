package com.grahambartley.toomanychests.lock;

import net.minecraft.entity.player.PlayerEntity;

/**
 * Applies a lock toggle to a player, and is the only path a client request may take.
 *
 * <p>Writing the attachment is what syncs the new set back to the owning client, so a rejected
 * index deliberately skips the write and leaves the client's mirror untouched.
 */
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
