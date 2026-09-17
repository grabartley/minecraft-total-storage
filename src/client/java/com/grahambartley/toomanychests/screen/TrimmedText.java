package com.grahambartley.toomanychests.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Language;

public final class TrimmedText {
  private TrimmedText() {}

  public static OrderedText toWidth(TextRenderer textRenderer, Text text, int width) {
    return Language.getInstance()
        .reorder(EllipsisTrim.toWidth(text, width, new RendererRoom(textRenderer)));
  }

  private record RendererRoom(TextRenderer textRenderer) implements EllipsisTrim.TextRoom {
    @Override
    public int widthOf(StringVisitable text) {
      return textRenderer.getWidth(text);
    }

    @Override
    public StringVisitable trimmedTo(StringVisitable text, int width) {
      return textRenderer.trimToWidth(text, width);
    }
  }
}
