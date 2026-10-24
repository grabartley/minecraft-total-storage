package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.FilterColumnLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;

public final class TagPromptLayout {
  public static final int NO_ROW = -1;

  public static final int WIDTH = TerminalScreenLayout.GRID_WIDTH;

  public static final int PADDING = 6;

  public static final int LINE_HEIGHT = 9;

  public static final int GAP = 3;

  public static final int ROW_HEIGHT = 12;

  public static final int VISIBLE_ROWS = 4;

  public static final int FOOTER_LINES = 2;

  public static final int HEADING_HEIGHT = LINE_HEIGHT + GAP;

  public static final int FOOTER_HEIGHT = GAP + FOOTER_LINES * LINE_HEIGHT;

  public static final int TRACK_HEIGHT = VISIBLE_ROWS * ROW_HEIGHT;

  public static final int HEIGHT = 2 * PADDING + HEADING_HEIGHT + TRACK_HEIGHT + FOOTER_HEIGHT;

  public static final int TEXT_WIDTH = WIDTH - 2 * PADDING;

  public static final int SCROLLBAR_WIDTH = FilterColumnLayout.SCROLLBAR_WIDTH;

  public static final int SCROLLBAR_GAP = 2;

  public static final int ROW_WIDTH = TEXT_WIDTH - SCROLLBAR_GAP - SCROLLBAR_WIDTH;

  public static final int CHECK_SIZE = 8;

  public static final int CHECK_BORDER = 1;

  public static final int TICK_SIZE = CHECK_SIZE - 2 * CHECK_BORDER;

  public static final int TEXT_MARGIN = 3;

  private TagPromptLayout() {}

  public static int left() {
    return TerminalScreenLayout.GRID_X;
  }

  public static int top() {
    return TerminalScreenLayout.INSET_Y + (TerminalScreenLayout.INSET_HEIGHT - HEIGHT) / 2;
  }

  public static int textLeft() {
    return left() + PADDING;
  }

  public static int headingTop() {
    return top() + PADDING;
  }

  public static int rowsTop() {
    return headingTop() + HEADING_HEIGHT;
  }

  public static int footerTop() {
    return top() + HEIGHT - PADDING - FOOTER_LINES * LINE_HEIGHT;
  }

  public static int scrollbarX() {
    return textLeft() + ROW_WIDTH + SCROLLBAR_GAP;
  }

  public static int rowY(int row) {
    if (row < 0 || row >= VISIBLE_ROWS) {
      throw new IndexOutOfBoundsException("no tag prompt row at " + row);
    }
    return rowsTop() + row * ROW_HEIGHT;
  }

  public static int rowAt(int windowX, int windowY) {
    if (windowX < textLeft() || windowX >= textLeft() + ROW_WIDTH) {
      return NO_ROW;
    }
    final int row = Math.floorDiv(windowY - rowsTop(), ROW_HEIGHT);
    return windowY < rowsTop() || row >= VISIBLE_ROWS ? NO_ROW : row;
  }

  public static boolean isOverScrollbar(int windowX, int windowY) {
    return windowX >= scrollbarX()
        && windowX < scrollbarX() + SCROLLBAR_WIDTH
        && windowY >= rowsTop()
        && windowY < rowsTop() + TRACK_HEIGHT;
  }

  public static boolean isOverPrompt(int windowX, int windowY) {
    return windowX >= left()
        && windowX < left() + WIDTH
        && windowY >= top()
        && windowY < top() + HEIGHT;
  }
}
