package com.grahambartley.totalstorage.screen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;

class PromptHeaderTest {
  private static final int X = 30;
  private static final int Y = 50;
  private static final int WIDTH = 150;
  private static final int TEXT_Y = Y + (PromptHeader.HEIGHT - 8) / 2;
  private static final Text TITLE = Text.literal("Rename chest");

  private static TextRenderer textRenderer() {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.getTextHandler()).thenReturn(new TextHandler((codePoint, style) -> 6.0f));
    return textRenderer;
  }

  private static void verifyTitleAt(DrawContext context, TextRenderer textRenderer, int x, int y) {
    verify(context)
        .drawText(
            eq(textRenderer),
            any(OrderedText.class),
            eq(x),
            eq(y),
            eq(VanillaPalette.opaque(VanillaPalette.LABEL)),
            eq(false));
  }

  @Test
  void aHeaderWithoutAnItemStartsItsTitleAtTheLeftEdge() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();

    PromptHeader.draw(context, textRenderer, null, TITLE, X, Y, WIDTH);

    verifyTitleAt(context, textRenderer, X, TEXT_Y);
    verify(context, never())
        .drawGuiTexture(eq(VanillaSprite.SLOT.id()), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void aBareTitleIsDrawnExactlyWhereItIsAsked() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();

    PromptHeader.drawTitle(context, textRenderer, TITLE, X, Y, WIDTH);

    verifyTitleAt(context, textRenderer, X, Y);
  }
}
