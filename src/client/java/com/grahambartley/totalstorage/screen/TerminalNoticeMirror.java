package com.grahambartley.totalstorage.screen;

import net.minecraft.text.Text;

public final class TerminalNoticeMirror {
  public static final int SHOWN_TICKS = 100;

  public static final int ANSWER_TICKS = 200;

  private static final int NO_SCREEN = -1;

  private static Text message;
  private static boolean needsAnAnswer;
  private static int syncId = NO_SCREEN;
  private static int ticksLeft;

  private TerminalNoticeMirror() {}

  public static void accept(int noticedSyncId, Text noticed, boolean answerNeeded) {
    if (noticed == null) {
      return;
    }
    syncId = noticedSyncId;
    message = noticed;
    needsAnAnswer = answerNeeded;
    ticksLeft = answerNeeded ? ANSWER_TICKS : SHOWN_TICKS;
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
    message = null;
    needsAnAnswer = false;
    syncId = NO_SCREEN;
    ticksLeft = 0;
  }

  public static boolean isShowingOn(int openSyncId) {
    return message != null && syncId == openSyncId && ticksLeft > 0;
  }

  public static Text message() {
    return message;
  }

  public static boolean needsAnAnswer() {
    return needsAnAnswer;
  }

  public static int ticksLeft() {
    return ticksLeft;
  }
}
