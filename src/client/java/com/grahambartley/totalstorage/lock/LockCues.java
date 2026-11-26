package com.grahambartley.totalstorage.lock;

import com.grahambartley.totalstorage.screen.TerminalScreen;
import net.minecraft.client.gui.screen.Screen;

public final class LockCues {
  private LockCues() {}

  public static boolean show(Screen screen, boolean locked) {
    return locked && screen instanceof TerminalScreen;
  }
}
