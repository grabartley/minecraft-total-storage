package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.operation.TerminalNoticeTone;
import net.minecraft.text.Text;

public final class TerminalNoticeMirror {
  public static final int SHOWN_TICKS = 100;

  private static final int NO_SCREEN = -1;

  private static TerminalNoticeTone tone = TerminalNoticeTone.DONE;
  private static Text headline;
  private static Text message;
  private static int syncId = NO_SCREEN;
  private static int ticksLeft;

  private TerminalNoticeMirror() {}

  public static void accept(
      int noticedSyncId, TerminalNoticeTone noticedTone, Text noticedHeadline, Text noticed) {
    if (noticed == null || noticedTone == null || !noticedTone.isShownInTheTerminal()) {
      return;
    }
    syncId = noticedSyncId;
    tone = noticedTone;
    message = noticed;
    headline = noticedHeadline == null ? noticed : noticedHeadline;
    ticksLeft = SHOWN_TICKS;
  }

  public static void tick() {
    if (ticksLeft > 0) {
      ticksLeft--;
    }
    if (ticksLeft == 0) {
      clear();
    }
  }

  public static void clear() {
    tone = TerminalNoticeTone.DONE;
    headline = null;
    message = null;
    syncId = NO_SCREEN;
    ticksLeft = 0;
  }

  public static boolean isShowingOn(int openSyncId) {
    return message != null && syncId == openSyncId && ticksLeft > 0;
  }

  public static TerminalNoticeTone tone() {
    return tone;
  }

  public static Text headline() {
    return headline;
  }

  public static Text message() {
    return message;
  }

  public static int ticksLeft() {
    return ticksLeft;
  }
}
