package com.grahambartley.totalstorage.screen;

import net.minecraft.client.gui.DrawContext;

public final class WarningGlyph {
  public static final int WIDTH = 2;

  public static final int HEIGHT = 7;

  public static final int BAR_HEIGHT = 4;

  public static final int GAP = 4;

  private WarningGlyph() {}

  public static void draw(DrawContext context, int x, int y, int colour) {
    TerminalChrome.fill(context, x, y, WIDTH, BAR_HEIGHT, colour);
    TerminalChrome.fill(context, x, y + BAR_HEIGHT + 1, WIDTH, HEIGHT - BAR_HEIGHT - 1, colour);
  }
}
