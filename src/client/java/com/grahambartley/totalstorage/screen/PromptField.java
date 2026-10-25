package com.grahambartley.totalstorage.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public final class PromptField {
  public static final int HEIGHT = 14;

  public static final int TEXT_INSET = 4;

  public static final String CARET = "_";

  private static final long BLINK_MILLIS = 300L;

  private PromptField() {}

  public static boolean caretShown(long millis) {
    return millis / BLINK_MILLIS % 2L == 0L;
  }

  public static String visibleTail(TextRenderer textRenderer, String typed, int room) {
    if (textRenderer.getWidth(typed) <= room) {
      return typed;
    }
    return textRenderer.trimToWidth(typed, room, true);
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      int x,
      int y,
      int width,
      String typed,
      Text placeholder,
      Text trailing,
      boolean refused) {
    VanillaSprite.TEXT_FIELD_HIGHLIGHTED.draw(context, x, y, width, HEIGHT);
    if (refused) {
      TerminalChrome.fill(context, x, y, width, 1, VanillaPalette.FIELD_REFUSED);
      TerminalChrome.fill(context, x, y + HEIGHT - 1, width, 1, VanillaPalette.FIELD_REFUSED);
      TerminalChrome.fill(context, x, y + 1, 1, HEIGHT - 2, VanillaPalette.FIELD_REFUSED);
      TerminalChrome.fill(
          context, x + width - 1, y + 1, 1, HEIGHT - 2, VanillaPalette.FIELD_REFUSED);
    }
    final int textY = y + (HEIGHT - 8) / 2;
    final int trailingWidth = trailing == null ? 0 : textRenderer.getWidth(trailing) + TEXT_INSET;
    if (trailing != null) {
      context.drawText(
          textRenderer,
          trailing,
          x + width - TEXT_INSET - textRenderer.getWidth(trailing),
          textY,
          VanillaPalette.opaque(VanillaPalette.FIELD_HINT),
          false);
    }
    final int caretWidth = textRenderer.getWidth(CARET);
    final int room = width - 2 * TEXT_INSET - trailingWidth - caretWidth;
    final String shown = visibleTail(textRenderer, typed, room);
    int textX = x + TEXT_INSET;
    if (shown.isEmpty() && placeholder != null) {
      context.drawText(
          textRenderer,
          TrimmedText.toWidth(textRenderer, placeholder, room),
          textX,
          textY,
          VanillaPalette.opaque(VanillaPalette.FIELD_HINT),
          false);
    } else {
      textX =
          context.drawText(
              textRenderer,
              shown,
              textX,
              textY,
              VanillaPalette.opaque(VanillaPalette.FIELD_TEXT),
              true);
    }
    final boolean placeholderShown = shown.isEmpty() && placeholder != null;
    if (!placeholderShown && caretShown(Util.getMeasuringTimeMs())) {
      context.drawText(
          textRenderer,
          CARET,
          shown.isEmpty() ? x + TEXT_INSET : textX,
          textY,
          VanillaPalette.opaque(VanillaPalette.FIELD_TEXT),
          true);
    }
  }

  public static void drawRefusal(
      DrawContext context, TextRenderer textRenderer, Text reason, int x, int y, int width) {
    WarningGlyph.draw(context, x, y, VanillaPalette.FAILURE);
    final int textX = x + WarningGlyph.WIDTH + WarningGlyph.GAP;
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, reason, x + width - textX),
        textX,
        y,
        VanillaPalette.opaque(VanillaPalette.FAILURE),
        false);
  }
}
