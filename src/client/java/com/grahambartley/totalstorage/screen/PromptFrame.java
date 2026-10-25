package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import net.minecraft.client.gui.DrawContext;

public record PromptFrame(int left, int top, int width, int height) {
  public static final int WIDTH = 180;

  public static final int PADDING_TOP = 7;

  public static final int PADDING_SIDE = 9;

  public static final int PADDING_BOTTOM = 9;

  public static final int CONTENT_WIDTH = WIDTH - 2 * PADDING_SIDE;

  public static final float ELEVATION = 400.0F;

  private static final int DIM_TOP = 0xC0101010;

  private static final int DIM_BOTTOM = 0xD0101010;

  public static PromptFrame centred(int contentHeight) {
    final int height = PADDING_TOP + contentHeight + PADDING_BOTTOM;
    return new PromptFrame(
        (TerminalScreenLayout.WINDOW_WIDTH - WIDTH) / 2,
        (TerminalScreenLayout.WINDOW_HEIGHT - height) / 2,
        WIDTH,
        height);
  }

  public int contentLeft() {
    return left + PADDING_SIDE;
  }

  public int contentTop() {
    return top + PADDING_TOP;
  }

  public int bottom() {
    return top + height;
  }

  public boolean isInside(int windowX, int windowY) {
    return windowX >= left && windowX < left + width && windowY >= top && windowY < top + height;
  }

  public static void begin(DrawContext context) {
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    context.fillGradient(
        0, 0, context.getScaledWindowWidth(), context.getScaledWindowHeight(), DIM_TOP, DIM_BOTTOM);
  }

  public static void end(DrawContext context) {
    context.getMatrices().pop();
  }

  public void drawPanel(DrawContext context, int originX, int originY) {
    TerminalSprite.PANEL.draw(context, originX + left, originY + top, width, height);
  }
}
