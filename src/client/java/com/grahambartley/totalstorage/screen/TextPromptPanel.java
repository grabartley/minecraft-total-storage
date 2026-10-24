package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class TextPromptPanel {
  private static final int WIDTH = 140;
  private static final int PADDING = 8;
  private static final int LINE_HEIGHT = 9;
  private static final int FIELD_HEIGHT = 12;
  private static final int GAP = 4;

  private static final float ELEVATION = 300.0F;

  private TextPromptPanel() {}

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      Text heading,
      Text hint,
      int hintColour,
      String typed,
      int originX,
      int originY) {
    final int inner = WIDTH - 2 * PADDING;
    final List<OrderedText> headingLines = textRenderer.wrapLines(heading, inner);
    final List<OrderedText> hintLines = textRenderer.wrapLines(hint, inner);
    final int height =
        2 * PADDING
            + 2 * GAP
            + FIELD_HEIGHT
            + LINE_HEIGHT * (headingLines.size() + hintLines.size());
    final int left =
        originX + TerminalScreenLayout.GRID_X + (TerminalScreenLayout.GRID_WIDTH - WIDTH) / 2;
    final int top =
        originY + TerminalScreenLayout.GRID_Y + (TerminalScreenLayout.GRID_HEIGHT - height) / 2;

    context.getMatrices().push();
    context.getMatrices().translate(0.0F, 0.0F, ELEVATION);
    TerminalSprite.PANEL.draw(context, left, top, WIDTH, height);
    int lineY = top + PADDING;
    lineY = drawLines(context, textRenderer, headingLines, left, lineY, VanillaPalette.LABEL) + GAP;
    TerminalSprite.FIELD_FOCUSED.draw(context, left + PADDING, lineY, inner, FIELD_HEIGHT);
    drawLines(
        context,
        textRenderer,
        List.of(Text.literal(typed).asOrderedText()),
        left,
        lineY + 2,
        VanillaPalette.FIELD_TEXT);
    drawLines(context, textRenderer, hintLines, left, lineY + FIELD_HEIGHT + GAP, hintColour);
    context.getMatrices().pop();
  }

  private static int drawLines(
      DrawContext context,
      TextRenderer textRenderer,
      List<OrderedText> lines,
      int left,
      int top,
      int colour) {
    int lineY = top;
    for (final OrderedText line : lines) {
      context.drawText(
          textRenderer,
          line,
          left + (WIDTH - textRenderer.getWidth(line)) / 2,
          lineY,
          colour,
          false);
      lineY += LINE_HEIGHT;
    }
    return lineY;
  }
}
