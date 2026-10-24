package com.grahambartley.totalstorage.screen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import net.minecraft.util.math.MathHelper;

public final class TrimmedText {
  private TrimmedText() {}

  public static OrderedText toWidth(TextRenderer textRenderer, Text text, int width) {
    return Language.getInstance().reorder(EllipsisTrim.toWidth(text, width, roomOf(textRenderer)));
  }

  public static List<OrderedText> toLines(
      TextRenderer textRenderer, String text, int width, int maxLines) {
    return Language.getInstance()
        .reorder(EllipsisTrim.toLines(text, width, maxLines, roomOf(textRenderer)));
  }

  private static EllipsisTrim.TextRoom roomOf(TextRenderer textRenderer) {
    return new HandlerRoom(textRenderer.getTextHandler());
  }

  record HandlerRoom(TextHandler handler) implements EllipsisTrim.TextRoom {
    @Override
    public int widthOf(StringVisitable text) {
      return MathHelper.ceil(handler.getWidth(text));
    }

    @Override
    public StringVisitable trimmedTo(StringVisitable text, int width) {
      return handler.trimToWidth(text, width, Style.EMPTY);
    }

    @Override
    public List<Integer> lineStarts(String text, int width) {
      final List<Integer> starts = new ArrayList<>();
      handler.wrapLines(text, width, Style.EMPTY, false, (style, start, end) -> starts.add(start));
      return starts;
    }
  }
}
