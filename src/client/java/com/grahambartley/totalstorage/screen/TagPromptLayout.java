package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;

public final class TagPromptLayout {
  public static final int NO_ROW = PromptListGeometry.NO_ROW;

  public static final int HEADER_HEIGHT = TerminalScreenLayout.SLOT_SIZE;

  public static final int HEADER_GAP = 4;

  public static final int ROW_HEIGHT = 13;

  public static final int VISIBLE_ROWS = 4;

  public static final int KEYS_GAP = 5;

  public static final int SCROLLBAR_WIDTH = 5;

  public static final int SCROLLBAR_GAP = 2;

  public static final int CHECK_SIZE = 11;

  public static final int TICK_SIZE = 6;

  public static final int TEXT_MARGIN = 4;

  public static final int ROW_WIDTH = PromptFrame.CONTENT_WIDTH - SCROLLBAR_GAP - SCROLLBAR_WIDTH;

  public static final PromptFrame FRAME =
      PromptFrame.centred(
          HEADER_HEIGHT + HEADER_GAP + VISIBLE_ROWS * ROW_HEIGHT + KEYS_GAP + KeyCaps.HEIGHT);

  public static final PromptListGeometry LIST =
      new PromptListGeometry(
          FRAME.contentLeft(),
          FRAME.contentTop() + HEADER_HEIGHT + HEADER_GAP,
          ROW_WIDTH,
          ROW_HEIGHT,
          VISIBLE_ROWS,
          FRAME.contentLeft() + ROW_WIDTH + SCROLLBAR_GAP,
          SCROLLBAR_WIDTH);

  public static final int WIDTH = FRAME.width();

  public static final int HEIGHT = FRAME.height();

  private TagPromptLayout() {}

  public static int headerTop() {
    return FRAME.contentTop();
  }

  public static int keysTop() {
    return LIST.top() + LIST.trackHeight() + KEYS_GAP;
  }

  public static boolean isOverPrompt(int windowX, int windowY) {
    return FRAME.isInside(windowX, windowY);
  }
}
