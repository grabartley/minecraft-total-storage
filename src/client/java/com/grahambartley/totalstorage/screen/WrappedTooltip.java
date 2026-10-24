package com.grahambartley.totalstorage.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class WrappedTooltip {
  public static final int MAX_WIDTH = 200;

  private WrappedTooltip() {}

  public static int widthFor(int scaledScreenWidth) {
    return Math.max(1, Math.min(MAX_WIDTH, scaledScreenWidth / 2));
  }

  public static List<OrderedText> wrap(
      List<Text> lines, int width, BiFunction<Text, Integer, List<OrderedText>> wrapper) {
    final List<OrderedText> wrapped = new ArrayList<>();
    for (final Text line : lines) {
      final List<OrderedText> parts = wrapper.apply(line, width);
      if (parts.isEmpty()) {
        wrapped.add(OrderedText.EMPTY);
      } else {
        wrapped.addAll(parts);
      }
    }
    return List.copyOf(wrapped);
  }

  public static void draw(
      DrawContext context, TextRenderer textRenderer, List<Text> lines, int mouseX, int mouseY) {
    context.drawOrderedTooltip(
        textRenderer,
        wrap(lines, widthFor(context.getScaledWindowWidth()), textRenderer::wrapLines),
        mouseX,
        mouseY);
  }

  public static void draw(
      DrawContext context, TextRenderer textRenderer, Text line, int mouseX, int mouseY) {
    draw(context, textRenderer, List.of(line), mouseX, mouseY);
  }
}
