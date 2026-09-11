package com.grahambartley.toomanychests.screen;

import net.minecraft.client.gui.DrawContext;

public final class TerminalRowChrome {
  public static final int ACCENT_INSET = 2;

  public static final int ACCENT_WIDTH = 2;

  private TerminalRowChrome() {}

  public static void draw(
      DrawContext context, boolean active, boolean hovered, int x, int y, int width, int height) {
    (hovered ? TerminalSprite.RAISED_LIT : TerminalSprite.FLAT).draw(context, x, y, width, height);
    if (!active) {
      return;
    }
    context.fill(
        x + ACCENT_INSET,
        y + ACCENT_INSET,
        x + ACCENT_INSET + ACCENT_WIDTH,
        y + height - ACCENT_INSET,
        0xFF000000 | VanillaPalette.ACCENT);
  }
}
