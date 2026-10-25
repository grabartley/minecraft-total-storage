package com.grahambartley.totalstorage.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TagCheckRow {
  private TagCheckRow() {}

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      Text name,
      boolean checked,
      boolean hovered,
      int left,
      int top) {
    if (hovered) {
      TerminalChrome.drawHover(
          context, left, top, TagPromptLayout.ROW_WIDTH, TagPromptLayout.ROW_HEIGHT);
    }
    final int boxY = top + (TagPromptLayout.ROW_HEIGHT - TagPromptLayout.CHECK_SIZE) / 2;
    VanillaSprite.TEXT_FIELD.draw(
        context, left + 1, boxY, TagPromptLayout.CHECK_SIZE, TagPromptLayout.CHECK_SIZE);
    if (checked) {
      final int inset = (TagPromptLayout.CHECK_SIZE - TagPromptLayout.TICK_SIZE + 1) / 2;
      TerminalSprite.CHECK_LIGHT.draw(
          context,
          left + 1 + inset,
          boxY + inset,
          TagPromptLayout.TICK_SIZE,
          TagPromptLayout.TICK_SIZE);
    }
    final int textLeft = left + 1 + TagPromptLayout.CHECK_SIZE + TagPromptLayout.TEXT_MARGIN;
    final OrderedText trimmed =
        TrimmedText.toWidth(textRenderer, name, left + TagPromptLayout.ROW_WIDTH - 2 - textLeft);
    context.drawText(
        textRenderer,
        trimmed,
        textLeft,
        top + (TagPromptLayout.ROW_HEIGHT - 8) / 2 + 1,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
  }
}
