package com.grahambartley.totalstorage.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class ButtonFace {
  public static final int FACE_INSET = 2;

  public static final int TEXT_MARGIN = 2;

  private static final int LINE_HEIGHT = 8;

  private ButtonFace() {}

  public static int labelRoom(int width) {
    return width - 2 * TEXT_MARGIN;
  }

  public static void drawAccent(DrawContext context, int x, int y, int width, int height) {
    TerminalChrome.fill(
        context,
        x + FACE_INSET,
        y + FACE_INSET,
        width - 2 * FACE_INSET,
        height - 2 * FACE_INSET - 1,
        VanillaPalette.ACCENT);
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      Text label,
      int x,
      int y,
      int width,
      int height,
      boolean active,
      boolean hovered,
      boolean accent) {
    draw(
        context,
        textRenderer,
        label,
        x,
        y,
        width,
        height,
        active,
        hovered,
        accent,
        active ? VanillaPalette.BUTTON_TEXT : VanillaPalette.BUTTON_TEXT_DISABLED);
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      Text label,
      int x,
      int y,
      int width,
      int height,
      boolean active,
      boolean hovered,
      boolean accent,
      int textColour) {
    VanillaSprite.button(active, hovered).draw(context, x, y, width, height);
    if (accent && active) {
      drawAccent(context, x, y, width, height);
    }
    final OrderedText shown = TrimmedText.toWidth(textRenderer, label, labelRoom(width));
    context.drawText(
        textRenderer,
        shown,
        x + (width - textRenderer.getWidth(shown) + 1) / 2,
        y + (height - LINE_HEIGHT) / 2,
        VanillaPalette.opaque(textColour),
        true);
  }
}
