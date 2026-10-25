package com.grahambartley.totalstorage.screen;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TagPromptFrame {
  private static final int LINE_HEIGHT = 9;

  private TagPromptFrame() {}

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      ItemStack item,
      Text heading,
      TagPromptList list,
      HintLine keys,
      Text note,
      Text empty,
      int originX,
      int originY,
      Runnable rows) {
    final PromptFrame frame = TagPromptLayout.FRAME;
    PromptFrame.begin(context);
    frame.drawPanel(context, originX, originY);
    PromptHeader.draw(
        context,
        textRenderer,
        item,
        heading,
        originX + frame.contentLeft(),
        originY + TagPromptLayout.headerTop(),
        PromptFrame.CONTENT_WIDTH);
    rows.run();
    if (empty != null) {
      drawEmpty(context, textRenderer, empty, originX, originY);
    } else {
      list.drawScrollbar(context, originX, originY);
    }
    drawKeys(context, textRenderer, keys, note, originX, originY);
    PromptFrame.end(context);
  }

  private static void drawEmpty(
      DrawContext context, TextRenderer textRenderer, Text empty, int originX, int originY) {
    final PromptListGeometry geometry = TagPromptLayout.LIST;
    final List<OrderedText> lines = textRenderer.wrapLines(empty, PromptFrame.CONTENT_WIDTH - 8);
    int top = originY + geometry.top() + (geometry.trackHeight() - lines.size() * LINE_HEIGHT) / 2;
    final int centre =
        originX + TagPromptLayout.FRAME.contentLeft() + PromptFrame.CONTENT_WIDTH / 2;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          centre - textRenderer.getWidth(line) / 2,
          top,
          VanillaPalette.opaque(VanillaPalette.MUTED),
          false);
      top += LINE_HEIGHT;
    }
  }

  private static void drawKeys(
      DrawContext context,
      TextRenderer textRenderer,
      HintLine keys,
      Text note,
      int originX,
      int originY) {
    final int left = originX + TagPromptLayout.FRAME.contentLeft();
    final int top = originY + TagPromptLayout.keysTop();
    final int keysWidth = keys == null ? 0 : KeyCaps.width(textRenderer, keys);
    if (keys != null) {
      KeyCaps.draw(context, textRenderer, keys, left, top, VanillaPalette.MUTED, false);
    }
    if (note == null) {
      return;
    }
    final int room = PromptFrame.CONTENT_WIDTH - (keys == null ? 0 : keysWidth + 8);
    final var shown = TrimmedText.toWidth(textRenderer, note, room);
    context.drawText(
        textRenderer,
        shown,
        left + PromptFrame.CONTENT_WIDTH - textRenderer.getWidth(shown),
        top + KeyCaps.TEXT_INSET,
        VanillaPalette.opaque(VanillaPalette.MUTED),
        false);
  }
}
