package com.grahambartley.toomanychests.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public final class TrimmedText {
  private TrimmedText() {}

  public static String toWidth(TextRenderer textRenderer, Text text, int width) {
    return EllipsisTrim.toWidth(text.getString(), width, textRenderer::getWidth);
  }
}
