package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.operation.TerminalNoticeTone;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TerminalStatusLine {
  private TerminalStatusLine() {}

  public static int colourOf(TerminalNoticeTone tone) {
    return tone == TerminalNoticeTone.FAILED ? VanillaPalette.FAILURE : VanillaPalette.LABEL;
  }

  public static int textLeft() {
    return TerminalScreenLayout.STATUS_X + WarningGlyph.WIDTH + WarningGlyph.GAP;
  }

  public static int textWidth() {
    return TerminalScreenLayout.STATUS_X + TerminalScreenLayout.STATUS_WIDTH - textLeft();
  }

  public static void draw(
      DrawContext context, TextRenderer textRenderer, TerminalNoticeTone tone, Text headline) {
    final int colour = colourOf(tone);
    WarningGlyph.draw(
        context, TerminalScreenLayout.STATUS_X, TerminalScreenLayout.STATUS_Y, colour);
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, headline, textWidth()),
        textLeft(),
        TerminalScreenLayout.STATUS_Y,
        VanillaPalette.opaque(colour),
        false);
  }

  public static boolean needsTooltip(TextRenderer textRenderer, Text headline, Text message) {
    return !headline.equals(message) || textRenderer.getWidth(headline) > textWidth();
  }

  public static void drawTooltip(
      DrawContext context, TextRenderer textRenderer, Text message, int mouseX, int mouseY) {
    HintTooltip.draw(context, textRenderer, List.of(message), List.of(), mouseX, mouseY);
  }
}
