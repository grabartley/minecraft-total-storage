package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public final class PromptHeader {
  public static final int HEIGHT = TerminalScreenLayout.SLOT_SIZE;

  public static final int GAP = 5;

  private PromptHeader() {}

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      ItemStack item,
      Text title,
      int x,
      int y,
      int width) {
    int textX = x;
    if (item != null && !item.isEmpty()) {
      VanillaSprite.SLOT.draw(context, x, y, HEIGHT, HEIGHT);
      context.drawItem(item, x + 1, y + 1);
      textX = x + HEIGHT + GAP;
    }
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, title, x + width - textX),
        textX,
        y + (HEIGHT - 8) / 2,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
  }

  public static void drawTitle(
      DrawContext context, TextRenderer textRenderer, Text title, int x, int y, int width) {
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, title, width),
        x,
        y,
        VanillaPalette.opaque(VanillaPalette.LABEL),
        false);
  }
}
