package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PromptFieldTest {
  private static final int GLYPH_WIDTH = 6;
  private static final int X = 20;
  private static final int Y = 40;
  private static final int WIDTH = 100;
  private static final int TEXT_Y = Y + (PromptField.HEIGHT - 8) / 2;

  private static final Text PLACEHOLDER = Text.literal("Name");
  private static final Text TRAILING = Text.literal("3/32");

  private static TextRenderer textRenderer() {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.getWidth(anyString()))
        .thenAnswer(invocation -> invocation.<String>getArgument(0).length() * GLYPH_WIDTH);
    when(textRenderer.getWidth(any(StringVisitable.class)))
        .thenAnswer(
            invocation ->
                invocation.<StringVisitable>getArgument(0).getString().length() * GLYPH_WIDTH);
    when(textRenderer.getTextHandler())
        .thenReturn(new TextHandler((codePoint, style) -> GLYPH_WIDTH));
    return textRenderer;
  }

  @ParameterizedTest(name = "{0} ms shown={1}")
  @CsvSource({"0, true", "299, true", "300, false", "599, false", "600, true"})
  void theCaretBlinksEveryThreeHundredMilliseconds(long millis, boolean shown) {
    assertEquals(shown, PromptField.caretShown(millis));
  }

  @Test
  void typingThatFitsIsShownWhole() {
    assertEquals("abc", PromptField.visibleTail(textRenderer(), "abc", 18));
  }

  @Test
  void typingThatDoesNotFitShowsOnlyItsEnd() {
    final TextRenderer textRenderer = textRenderer();
    when(textRenderer.trimToWidth("abcdefgh", 30, true)).thenReturn("defgh");

    assertEquals("defgh", PromptField.visibleTail(textRenderer, "abcdefgh", 30));
  }

  @Test
  void theFieldIsTheHighlightedVanillaTextField() {
    final DrawContext context = mock(DrawContext.class);

    PromptField.draw(context, textRenderer(), X, Y, WIDTH, "", null, null, false);

    verify(context)
        .drawGuiTexture(VanillaSprite.TEXT_FIELD_HIGHLIGHTED.id(), X, Y, WIDTH, PromptField.HEIGHT);
  }

  @ParameterizedTest(name = "refused={0}")
  @CsvSource({"true, 4", "false, 0"})
  void aRefusedFieldIsOutlinedOnAllFourSides(boolean refused, int edges) {
    final DrawContext context = mock(DrawContext.class);

    PromptField.draw(context, textRenderer(), X, Y, WIDTH, "abc", null, null, refused);

    verify(context, times(edges))
        .fill(
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            eq(VanillaPalette.opaque(VanillaPalette.FIELD_REFUSED)));
  }

  @Test
  void typedTextIsDrawnShadowedFromTheInset() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();

    PromptField.draw(context, textRenderer, X, Y, WIDTH, "abc", PLACEHOLDER, null, false);

    verify(context)
        .drawText(
            textRenderer,
            "abc",
            X + PromptField.TEXT_INSET,
            TEXT_Y,
            VanillaPalette.opaque(VanillaPalette.FIELD_TEXT),
            true);
    verify(context, never())
        .drawText(any(), any(OrderedText.class), anyInt(), anyInt(), anyInt(), anyBoolean());
  }

  @Test
  void anEmptyFieldShowsThePlaceholderInGreyWithoutACaret() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();

    PromptField.draw(context, textRenderer, X, Y, WIDTH, "", PLACEHOLDER, null, false);

    verify(context)
        .drawText(
            eq(textRenderer),
            any(OrderedText.class),
            eq(X + PromptField.TEXT_INSET),
            eq(TEXT_Y),
            eq(VanillaPalette.opaque(VanillaPalette.FIELD_HINT)),
            eq(false));
    verify(context, never())
        .drawText(any(TextRenderer.class), anyString(), anyInt(), anyInt(), anyInt(), anyBoolean());
  }

  @Test
  void trailingTextIsRightAlignedInsideTheField() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();

    PromptField.draw(context, textRenderer, X, Y, WIDTH, "abc", null, TRAILING, false);

    verify(context)
        .drawText(
            textRenderer,
            TRAILING,
            X + WIDTH - PromptField.TEXT_INSET - TRAILING.getString().length() * GLYPH_WIDTH,
            TEXT_Y,
            VanillaPalette.opaque(VanillaPalette.FIELD_HINT),
            false);
  }

  @Test
  void typingIsCutToTheRoomLeftByTheTrailingTextAndTheCaret() {
    final TextRenderer textRenderer = textRenderer();
    final String typed = "abcdefghijklmnop";
    final int room =
        WIDTH
            - 2 * PromptField.TEXT_INSET
            - (TRAILING.getString().length() * GLYPH_WIDTH + PromptField.TEXT_INSET)
            - GLYPH_WIDTH;
    when(textRenderer.trimToWidth(typed, room, true)).thenReturn("mnop");
    final DrawContext context = mock(DrawContext.class);

    PromptField.draw(context, textRenderer, X, Y, WIDTH, typed, null, TRAILING, false);

    verify(context)
        .drawText(
            textRenderer,
            "mnop",
            X + PromptField.TEXT_INSET,
            TEXT_Y,
            VanillaPalette.opaque(VanillaPalette.FIELD_TEXT),
            true);
  }

  @Test
  void aRefusalIsAWarningFollowedByTheReasonInRed() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();
    final int red = VanillaPalette.opaque(VanillaPalette.FAILURE);

    PromptField.drawRefusal(context, textRenderer, Text.literal("Taken"), X, Y, WIDTH);

    verify(context).fill(X, Y, X + WarningGlyph.WIDTH, Y + WarningGlyph.BAR_HEIGHT, red);
    verify(context)
        .drawText(
            eq(textRenderer),
            any(OrderedText.class),
            eq(X + WarningGlyph.WIDTH + WarningGlyph.GAP),
            eq(Y),
            eq(red),
            eq(false));
  }
}
