package com.grahambartley.toomanychests.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TagPromptFrame {
  private static final float ELEVATION = 300.0F;

  private TagPromptFrame() {}

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      Text heading,
      Text footer,
      TagPromptList list,
      int originX,
      int originY,
      Runnable rows) {
    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    TerminalSprite.PANEL.draw(
        context,
        originX + TagPromptLayout.left(),
        originY + TagPromptLayout.top(),
        TagPromptLayout.WIDTH,
        TagPromptLayout.HEIGHT);
    context.drawText(
        textRenderer,
        TrimmedText.toWidth(textRenderer, heading, TagPromptLayout.TEXT_WIDTH),
        originX + TagPromptLayout.textLeft(),
        originY + TagPromptLayout.headingTop(),
        VanillaPalette.LABEL,
        false);
    rows.run();
    list.drawScrollbar(context, originX, originY);
    drawFooter(context, textRenderer, footer, originX, originY);
    context.getMatrices().pop();
  }

  private static void drawFooter(
      DrawContext context, TextRenderer textRenderer, Text footer, int originX, int originY) {
    int lineY = originY + TagPromptLayout.footerTop();
    for (final OrderedText line :
        TrimmedText.toLines(
            textRenderer,
            footer.getString(),
            TagPromptLayout.TEXT_WIDTH,
            TagPromptLayout.FOOTER_LINES)) {
      context.drawText(
          textRenderer,
          line,
          originX + TagPromptLayout.textLeft(),
          lineY,
          VanillaPalette.MUTED,
          false);
      lineY += TagPromptLayout.LINE_HEIGHT;
    }
  }
}
