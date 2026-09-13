package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.screenhandler.TerminalScreenLayout;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TerminalNoticeBanner {
  private static final float ELEVATION = 300.0F;

  private TerminalNoticeBanner() {}

  public static int rowsCovered(TextRenderer textRenderer, Text message) {
    final int lines = linesOf(textRenderer, message).size();
    return lines == 0 ? 0 : TerminalScreenLayout.noticeRows(lines);
  }

  private static List<OrderedText> linesOf(TextRenderer textRenderer, Text message) {
    if (message == null) {
      return List.of();
    }
    return textRenderer.wrapLines(message, TerminalScreenLayout.NOTICE_TEXT_WIDTH).stream()
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
    if (message == null) {
      return;
    }
    final List<OrderedText> lines = linesOf(textRenderer, message);
    if (lines.isEmpty()) {
      return;
    }
    final int left = originX + TerminalScreenLayout.NOTICE_X;
    final int top = originY + TerminalScreenLayout.NOTICE_Y;
    final int height = TerminalScreenLayout.noticeHeight(lines.size());
    chromeFor(needsAnAnswer).draw(context, left, top, TerminalScreenLayout.NOTICE_WIDTH, height);
    int lineY = top + (height - lines.size() * TerminalScreenLayout.NOTICE_LINE_HEIGHT) / 2;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          left + (TerminalScreenLayout.NOTICE_WIDTH - textRenderer.getWidth(line)) / 2,
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
