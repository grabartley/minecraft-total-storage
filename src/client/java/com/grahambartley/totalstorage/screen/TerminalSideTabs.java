package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import net.minecraft.client.gui.DrawContext;

public final class TerminalSideTabs {
  public static final int X = TerminalScreenLayout.WINDOW_WIDTH;

  public static final int WIDTH = 25;

  public static final int HEIGHT = 24;

  public static final int OVERLAP = 3;

  public static final int SELECTED_X = X - OVERLAP;

  public static final int SELECTED_WIDTH = WIDTH + OVERLAP;

  public static final int SELECTED_HEIGHT = HEIGHT + 2;

  public static final int FIRST_Y = TerminalScreenLayout.HEADER_Y + 2;

  public static final int PITCH = HEIGHT + 4;

  public static final int ICON_SIZE = 12;

  public static final int ICON_X = X + (WIDTH - 3 - ICON_SIZE) / 2 + 1;

  private TerminalSideTabs() {}

  public static int top(TerminalTab tab) {
    return FIRST_Y + tab.index() * PITCH;
  }

  public static int iconY(TerminalTab tab) {
    return top(tab) + (HEIGHT - ICON_SIZE) / 2;
  }

  public static TerminalTab at(int windowX, int windowY) {
    if (windowX < SELECTED_X || windowX >= X + WIDTH) {
      return null;
    }
    for (final TerminalTab tab : TerminalTab.values()) {
      if (windowY >= top(tab) - 1 && windowY < top(tab) + HEIGHT + 1) {
        return tab;
      }
    }
    return null;
  }

  public static void draw(
      DrawContext context, TerminalTab selected, TerminalTab hovered, int originX, int originY) {
    for (final TerminalTab tab : TerminalTab.values()) {
      if (tab == selected) {
        continue;
      }
      TerminalChrome.drawSideTab(
          context, originX + X, originY + top(tab), WIDTH, HEIGHT, false, originX + X);
      drawIcon(context, tab, tab == hovered, originX, originY);
    }
  }

  public static void drawSelected(
      DrawContext context, TerminalTab selected, int originX, int originY) {
    TerminalChrome.drawSideTab(
        context,
        originX + SELECTED_X,
        originY + top(selected) - 1,
        SELECTED_WIDTH,
        SELECTED_HEIGHT,
        true,
        originX + X - 1);
    drawIcon(context, selected, false, originX, originY);
  }

  private static void drawIcon(
      DrawContext context, TerminalTab tab, boolean hovered, int originX, int originY) {
    if (hovered) {
      TerminalChrome.drawHover(context, originX + X, originY + top(tab) + 3, WIDTH - 3, HEIGHT - 6);
    }
    tab.icon().draw(context, originX + ICON_X, originY + iconY(tab), ICON_SIZE, ICON_SIZE);
  }
}
