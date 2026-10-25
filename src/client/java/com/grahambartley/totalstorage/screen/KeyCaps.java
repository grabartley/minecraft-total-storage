package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.lock.LockedSlotBadge;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class KeyCaps {
  public static final int HEIGHT = 12;

  public static final int TEXT_INSET = 2;

  public static final int PADDING = 3;

  public static final int GAP = 3;

  public static final int GROUP_GAP = 8;

  public static final int LINE_GAP = 3;

  private KeyCaps() {}

  public static int keyWidth(int labelWidth) {
    return labelWidth + 2 * PADDING + 2;
  }

  public static int partWidth(TextRenderer textRenderer, HintLine.Part part) {
    return switch (part) {
      case HintLine.Key key -> keyWidth(textRenderer.getWidth(key.label()));
      case HintLine.Words words -> textRenderer.getWidth(words.text());
      case HintLine.LockBadge badge -> LockedSlotBadge.WIDTH;
    };
  }

  public static int gapBefore(HintLine.Part previous, HintLine.Part next) {
    if (previous == null) {
      return 0;
    }
    return previous instanceof HintLine.Words && next instanceof HintLine.Key ? GROUP_GAP : GAP;
  }

  public static int width(TextRenderer textRenderer, HintLine line) {
    int width = 0;
    HintLine.Part previous = null;
    for (final HintLine.Part part : line.parts()) {
      width += gapBefore(previous, part) + partWidth(textRenderer, part);
      previous = part;
    }
    return width;
  }

  public static int width(TextRenderer textRenderer, List<HintLine> lines) {
    int widest = 0;
    for (final HintLine line : lines) {
      widest = Math.max(widest, width(textRenderer, line));
    }
    return widest;
  }

  public static int height(int lines) {
    return lines <= 0 ? 0 : lines * HEIGHT + (lines - 1) * LINE_GAP;
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      HintLine line,
      int x,
      int y,
      int wordsColour,
      boolean shadowedWords) {
    int left = x;
    HintLine.Part previous = null;
    for (final HintLine.Part part : line.parts()) {
      left += gapBefore(previous, part);
      previous = part;
      switch (part) {
        case HintLine.Key key -> drawKey(context, textRenderer, key, left, y);
        case HintLine.Words words ->
            context.drawText(
                textRenderer,
                words.text(),
                left,
                y + TEXT_INSET,
                VanillaPalette.opaque(wordsColour),
                shadowedWords);
        case HintLine.LockBadge badge ->
            LockedSlotBadge.drawFlat(context, left, y + (HEIGHT - LockedSlotBadge.HEIGHT) / 2);
      }
      left += partWidth(textRenderer, part);
    }
  }

  public static void drawLines(
      DrawContext context,
      TextRenderer textRenderer,
      List<HintLine> lines,
      int x,
      int y,
      int wordsColour,
      boolean shadowedWords) {
    int top = y;
    for (final HintLine line : lines) {
      draw(context, textRenderer, line, x, top, wordsColour, shadowedWords);
      top += HEIGHT + LINE_GAP;
    }
  }

  private static void drawKey(
      DrawContext context, TextRenderer textRenderer, HintLine.Key key, int x, int y) {
    final int width = keyWidth(textRenderer.getWidth(key.label()));
    TerminalChrome.fill(context, x, y, width, HEIGHT, VanillaPalette.BEVEL_SHADOW);
    TerminalChrome.fill(context, x + 1, y + 1, width - 2, HEIGHT - 2, VanillaPalette.PANEL);
    TerminalChrome.fill(context, x + 1, y + 1, width - 3, 1, VanillaPalette.BEVEL_HIGHLIGHT);
    TerminalChrome.fill(context, x + 1, y + 2, 1, HEIGHT - 4, VanillaPalette.BEVEL_HIGHLIGHT);
    TerminalChrome.fill(context, x + 2, y + HEIGHT - 2, width - 3, 1, VanillaPalette.SUNKEN_FILL);
    TerminalChrome.fill(context, x + width - 2, y + 2, 1, HEIGHT - 4, VanillaPalette.SUNKEN_FILL);
    context.drawText(
        textRenderer,
        key.label(),
        x + 1 + PADDING,
        y + TEXT_INSET,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
  }
}
