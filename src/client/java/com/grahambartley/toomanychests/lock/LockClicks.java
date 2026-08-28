package com.grahambartley.toomanychests.lock;

import com.grahambartley.toomanychests.network.ModNetworkingClient;
import net.minecraft.screen.slot.Slot;

/**
 * Turns a lock gesture on a slot into a request the server will decide.
 *
 * <p>A slot the client believes is unlockable is dropped here rather than sent, because the server
 * would only reject it, and a rejected request is a round trip that changes nothing.
 */
public final class LockClicks {
  private LockClicks() {}

  public static void requestToggle(Slot slot) {
    final int index = LockSlotTarget.lockableIndex(slot);
    if (index != LockSlotTarget.NONE) {
      ModNetworkingClient.sendLockToggle(index);
    }
  }
}
