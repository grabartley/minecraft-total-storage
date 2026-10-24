package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import com.grahambartley.totalstorage.screenhandler.TerminalSettingsLayout;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TerminalNoticeBanner {
  private TerminalNoticeBanner() {}

  public static int rowsCovered(TextRenderer textRenderer, Text message) {
    final int lines = linesOf(textRenderer, message, TerminalScreenLayout.NOTICE_TEXT_WIDTH).size();
    return lines == 0 ? 0 : TerminalScreenLayout.noticeRows(lines);
  }

  private static List<OrderedText> linesOf(TextRenderer textRenderer, Text message, int width) {
    if (message == null) {
      return List.of();
    }
    return textRenderer.wrapLines(message, width).stream()
        .limit(TerminalScreenLayout.NOTICE_MAX_LINES)
        .toList();
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      Text message,
      boolean needsAnAnswer,
      int originX,
      int originY) {
    drawAt(
        context,
        textRenderer,
        message,
        needsAnAnswer,
        originX + TerminalScreenLayout.NOTICE_X,
        originY + TerminalScreenLayout.NOTICE_Y,
        TerminalScreenLayout.NOTICE_WIDTH);
  }

  public static void drawInSettings(
      DrawContext context,
      TextRenderer textRenderer,
      Text message,
      boolean needsAnAnswer,
      int originX,
      int originY) {
    drawAt(
        context,
        textRenderer,
        message,
        needsAnAnswer,
        originX + TerminalSettingsLayout.PANEL_X,
        originY + TerminalSettingsLayout.CONTENT_Y,
        TerminalSettingsLayout.PANEL_WIDTH);
  }

  private static void drawAt(
      DrawContext context,
      TextRenderer textRenderer,
      Text message,
      boolean needsAnAnswer,
      int left,
      int top,
      int width) {
    if (message == null) {
      return;
    }
    final List<OrderedText> lines =
        linesOf(textRenderer, message, width - 2 * TerminalScreenLayout.NOTICE_PADDING);
    if (lines.isEmpty()) {
      return;
    }
    final int height = TerminalScreenLayout.noticeHeight(lines.size());
    chromeFor(needsAnAnswer).draw(context, left, top, width, height);
    int lineY = top + (height - lines.size() * TerminalScreenLayout.NOTICE_LINE_HEIGHT) / 2;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          left + (width - textRenderer.getWidth(line)) / 2,
          lineY,
          VanillaPalette.LABEL,
          false);
      lineY += TerminalScreenLayout.NOTICE_LINE_HEIGHT;
    }
  }

  private static TerminalSprite chromeFor(boolean needsAnAnswer) {
    return needsAnAnswer ? TerminalSprite.RAISED_LIT : TerminalSprite.RAISED;
  }
}
