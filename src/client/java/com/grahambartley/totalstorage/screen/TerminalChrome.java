package com.grahambartley.totalstorage.screen;

import net.minecraft.client.gui.DrawContext;

public final class TerminalChrome {
  public static final int SCROLLER_WIDTH = 12;

  public static final int SCROLLER_HEIGHT = 15;

  private TerminalChrome() {}

  public static void drawScrollbar(
      DrawContext context, TerminalScrollbar scrollbar, int x, int y, int width, int height) {
    TerminalSprite.SUNKEN.draw(context, x, y, width, height);
    if (!scrollbar.isEnabled()) {
      return;
    }
    TerminalSprite.RAISED.draw(
        context, x, y + scrollbar.thumbTop(), width, scrollbar.thumbHeight());
  }

  public static void drawScroller(
      DrawContext context, TerminalScrollbar scrollbar, int x, int y, int height) {
    TerminalSprite.SUNKEN.draw(context, x, y, SCROLLER_WIDTH, height);
    (scrollbar.isEnabled() ? VanillaSprite.SCROLLER : VanillaSprite.SCROLLER_DISABLED)
        .draw(context, x, y + 1 + scrollbar.thumbTop(), SCROLLER_WIDTH, SCROLLER_HEIGHT);
  }

  public static void drawInset(DrawContext context, int x, int y, int width, int height) {
    fill(context, x, y, width - 1, 1, VanillaPalette.BEVEL_SHADOW);
    fill(context, x, y + 1, 1, height - 2, VanillaPalette.BEVEL_SHADOW);
    fill(context, x + 1, y + height - 1, width - 1, 1, VanillaPalette.BEVEL_HIGHLIGHT);
    fill(context, x + width - 1, y + 1, 1, height - 1, VanillaPalette.BEVEL_HIGHLIGHT);
  }

  public static void drawRaisedFrame(DrawContext context, int x, int y, int width, int height) {
    fill(context, x, y, width, 1, VanillaPalette.OUTLINE);
    fill(context, x, y + height - 1, width, 1, VanillaPalette.OUTLINE);
    fill(context, x, y + 1, 1, height - 2, VanillaPalette.OUTLINE);
    fill(context, x + width - 1, y + 1, 1, height - 2, VanillaPalette.OUTLINE);
    fill(context, x + 1, y + 1, width - 3, 1, VanillaPalette.BEVEL_HIGHLIGHT);
    fill(context, x + 1, y + 2, 1, height - 4, VanillaPalette.BEVEL_HIGHLIGHT);
    fill(context, x + 2, y + height - 2, width - 3, 1, VanillaPalette.BEVEL_SHADOW);
    fill(context, x + width - 2, y + 2, 1, height - 4, VanillaPalette.BEVEL_SHADOW);
    fill(context, x + 2, y + 2, width - 4, height - 4, VanillaPalette.PANEL);
  }

  public static void drawSideTab(
      DrawContext context, int x, int y, int width, int height, boolean selected, int joinX) {
    final int right = x + width - 1;
    final int bottom = y + height - 1;
    final int face = selected ? VanillaPalette.PANEL : VanillaPalette.SUNKEN_FILL;
    final int outlineFrom = selected ? joinX : x;
    fill(context, outlineFrom, y, right - outlineFrom, 1, VanillaPalette.OUTLINE);
    fill(context, outlineFrom, bottom, right - outlineFrom, 1, VanillaPalette.OUTLINE);
    fill(context, right, y + 1, 1, height - 2, VanillaPalette.OUTLINE);
    fill(context, x, y + 1, right - x, 2, VanillaPalette.BEVEL_HIGHLIGHT);
    fill(context, x, bottom - 2, right - x, 2, VanillaPalette.BEVEL_SHADOW);
    fill(context, right - 2, y + 3, 2, height - 5, VanillaPalette.BEVEL_SHADOW);
    fill(context, right - 2, y + 2, 1, 1, face);
    fill(context, x, y + 3, right - 2 - x, height - 6, face);
    if (selected) {
      fill(context, x, y, outlineFrom - x, 1, VanillaPalette.BEVEL_SHADOW);
      fill(context, x, bottom, outlineFrom - x, 1, VanillaPalette.BEVEL_SHADOW);
    }
  }

  public static void drawSectionTab(
      DrawContext context, int x, int y, int width, int height, boolean selected) {
    final int right = x + width - 1;
    final int face = selected ? VanillaPalette.PANEL : VanillaPalette.SUNKEN_FILL;
    fill(context, x + 1, y, width - 2, 1, VanillaPalette.OUTLINE);
    fill(context, x, y + 1, 1, height - 1, VanillaPalette.OUTLINE);
    fill(context, right, y + 1, 1, height - 1, VanillaPalette.OUTLINE);
    fill(context, x + 1, y + 1, width - 3, 1, VanillaPalette.BEVEL_HIGHLIGHT);
    fill(context, x + 1, y + 2, 1, height - 2, VanillaPalette.BEVEL_HIGHLIGHT);
    fill(context, right - 1, y + 2, 1, height - 2, VanillaPalette.BEVEL_SHADOW);
    fill(context, x + 2, y + 2, width - 4, height - 2, face);
  }

  public static void drawHover(DrawContext context, int x, int y, int width, int height) {
    context.fill(x, y, x + width, y + height, VanillaPalette.HOVER);
  }

  public static void fill(DrawContext context, int x, int y, int width, int height, int colour) {
    if (width <= 0 || height <= 0) {
      return;
    }
    context.fill(x, y, x + width, y + height, VanillaPalette.opaque(colour));
  }
}
