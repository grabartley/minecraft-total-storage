package com.grahambartley.toomanychests.screen;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TrimmedText {
  private TrimmedText() {}

  public static OrderedText toWidth(TextRenderer textRenderer, Text text, int width) {
    final List<OrderedText> lines = textRenderer.wrapLines(text, Math.max(1, width));
    return lines.isEmpty() ? OrderedText.EMPTY : lines.get(0);
  }
}
