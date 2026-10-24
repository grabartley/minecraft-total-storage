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
    TerminalChrome.drawRow(
        context, hovered, left, top, TagPromptLayout.ROW_WIDTH, TagPromptLayout.ROW_HEIGHT);
    final int checkInset = (TagPromptLayout.ROW_HEIGHT - TagPromptLayout.CHECK_SIZE) / 2;
    TerminalSprite.SUNKEN.draw(
        context,
        left + checkInset,
        top + checkInset,
        TagPromptLayout.CHECK_SIZE,
        TagPromptLayout.CHECK_SIZE);
    if (checked) {
      TerminalSprite.CHECK.draw(
          context,
          left + checkInset + TagPromptLayout.CHECK_BORDER,
          top + checkInset + TagPromptLayout.CHECK_BORDER,
          TagPromptLayout.TICK_SIZE,
          TagPromptLayout.TICK_SIZE);
    }
    final int textLeft =
        left + checkInset + TagPromptLayout.CHECK_SIZE + TagPromptLayout.TEXT_MARGIN;
    final OrderedText trimmed =
        TrimmedText.toWidth(textRenderer, name, left + TagPromptLayout.ROW_WIDTH - textLeft);
    context.drawText(
        textRenderer,
        trimmed,
        textLeft,
        top + (TagPromptLayout.ROW_HEIGHT - TagPromptLayout.LINE_HEIGHT) / 2 + 1,
        checked ? VanillaPalette.LABEL : VanillaPalette.MUTED,
        false);
  }
}
