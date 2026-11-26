package com.grahambartley.totalstorage.lock;

import com.grahambartley.totalstorage.screen.TerminalScreen;
import net.minecraft.client.gui.screen.Screen;

public final class LockCues {
  private LockCues() {}

  public static boolean belongOn(Screen screen) {
    return screen instanceof TerminalScreen;
  }
}
