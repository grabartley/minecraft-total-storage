package com.grahambartley.toomanychests.screen;

import net.minecraft.client.gui.DrawContext;

public final class TerminalChrome {
  public static final int ACCENT_INSET = 2;

  public static final int ACCENT_WIDTH = 2;

  private TerminalChrome() {}

  public static TerminalSprite control(boolean hovered) {
    return hovered ? TerminalSprite.RAISED_LIT : TerminalSprite.RAISED;
  }

  public static TerminalSprite unavailableControl() {
    return TerminalSprite.FLAT;
  }

  public static TerminalSprite controlOrUnavailable(boolean available, boolean hovered) {
    return available ? control(hovered) : unavailableControl();
  }

  public static TerminalSprite tab(boolean current, boolean hovered) {
    if (current) {
      return TerminalSprite.RAISED;
    }
    return hovered ? TerminalSprite.RAISED_LIT : TerminalSprite.PRESSED;
  }

  public static TerminalSprite row(boolean hovered) {
    return hovered ? TerminalSprite.RAISED_LIT : TerminalSprite.FLAT;
  }

  public static void drawRow(
      DrawContext context, boolean hovered, int x, int y, int width, int height) {
    row(hovered).draw(context, x, y, width, height);
  }

  public static void drawActiveRow(
      DrawContext context, boolean active, boolean hovered, int x, int y, int width, int height) {
    drawRow(context, hovered, x, y, width, height);
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
