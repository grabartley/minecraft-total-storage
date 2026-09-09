package com.grahambartley.toomanychests.lock;

import com.grahambartley.toomanychests.network.ModNetworkingClient;
import net.minecraft.screen.slot.Slot;

public final class LockClicks {
  private LockClicks() {}

  public static void requestToggle(Slot slot) {
    final int index = LockSlotTarget.lockableIndex(slot);
    if (index != LockSlotTarget.NONE) {
      ModNetworkingClient.sendLockToggle(index);
    }
  }
}
