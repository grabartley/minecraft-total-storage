package com.grahambartley.totalstorage.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class TextPromptPanel {
  public static final int LINE_HEIGHT = 9;

  public static final int GAP = 5;

  public static final int REFUSAL_GAP = 4;

  public static final int KEYS_GAP = 5;

  public static final PromptFrame FRAME =
      PromptFrame.centred(
          LINE_HEIGHT
              + GAP
              + PromptField.HEIGHT
              + REFUSAL_GAP
              + LINE_HEIGHT
              + KEYS_GAP
              + KeyCaps.HEIGHT);

  private TextPromptPanel() {}

  public static int refusalTop() {
    return FRAME.contentTop() + LINE_HEIGHT + GAP + PromptField.HEIGHT + REFUSAL_GAP;
  }

  public static boolean isOverRefusal(int windowX, int windowY) {
    return windowX >= FRAME.contentLeft()
        && windowX < FRAME.contentLeft() + PromptFrame.CONTENT_WIDTH
        && windowY >= refusalTop()
        && windowY < refusalTop() + LINE_HEIGHT;
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      Text heading,
      String typed,
      Text placeholder,
      Text refusal,
      HintLine keys,
      int originX,
      int originY) {
    final int left = originX + FRAME.contentLeft();
    int top = originY + FRAME.contentTop();
    PromptFrame.begin(context);
    FRAME.drawPanel(context, originX, originY);
    PromptHeader.drawTitle(context, textRenderer, heading, left, top, PromptFrame.CONTENT_WIDTH);
    top += LINE_HEIGHT + GAP;
    PromptField.draw(
        context,
        textRenderer,
        left,
        top,
        PromptFrame.CONTENT_WIDTH,
        typed,
        placeholder,
        null,
        refusal != null);
    top += PromptField.HEIGHT + REFUSAL_GAP;
    if (refusal != null) {
      PromptField.drawRefusal(context, textRenderer, refusal, left, top, PromptFrame.CONTENT_WIDTH);
    }
    top += LINE_HEIGHT + KEYS_GAP;
    KeyCaps.draw(context, textRenderer, keys, left, top, VanillaPalette.MUTED, false);
    PromptFrame.end(context);
  }

  public static HintLine saveKeys() {
    return HintLine.of(
        HintLine.key("enter"),
        HintLine.words("hint.save"),
        HintLine.key("esc"),
        HintLine.words("hint.cancel"));
  }
}
