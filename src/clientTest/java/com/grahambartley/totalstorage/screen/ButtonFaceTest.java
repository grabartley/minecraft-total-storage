package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ButtonFaceTest {
  private static final int X = 10;
  private static final int Y = 20;
  private static final int WIDTH = 46;
  private static final int HEIGHT = 14;
  private static final int LABEL_WIDTH = 30;

  private static TextRenderer textRenderer() {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.getTextHandler()).thenReturn(new TextHandler((codePoint, style) -> 1.0f));
    when(textRenderer.getWidth(any(OrderedText.class))).thenReturn(LABEL_WIDTH);
    return textRenderer;
  }

  private static DrawContext drawn(boolean active, boolean hovered, boolean accent) {
    final DrawContext context = mock(DrawContext.class);
    ButtonFace.draw(
        context,
        textRenderer(),
        Text.literal("Delete?"),
        X,
        Y,
        WIDTH,
        HEIGHT,
        active,
        hovered,
        accent);
    return context;
  }

  @ParameterizedTest(name = "active={0} hovered={1}")
  @CsvSource({
    "true, false, BUTTON",
    "true, true, BUTTON_HIGHLIGHTED",
    "false, false, BUTTON_DISABLED",
    "false, true, BUTTON_DISABLED"
  })
  void theFaceIsVanillasButtonForItsState(boolean active, boolean hovered, VanillaSprite sprite) {
    verify(drawn(active, hovered, false)).drawGuiTexture(sprite.id(), X, Y, WIDTH, HEIGHT);
  }

  @Test
  void anAccentFillsTheFaceInsideVanillasBevels() {
    verify(drawn(true, false, true))
        .fill(
            X + ButtonFace.FACE_INSET,
            Y + ButtonFace.FACE_INSET,
            X + WIDTH - ButtonFace.FACE_INSET,
            Y + HEIGHT - ButtonFace.FACE_INSET - 1,
            VanillaPalette.opaque(VanillaPalette.ACCENT));
  }

  @Test
  void aDisabledButtonDropsItsAccent() {
    verify(drawn(false, false, true), never())
        .fill(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @ParameterizedTest(name = "active={0}")
  @CsvSource({"true", "false"})
  void theLabelIsCentredOnTheWidthItIsDrawnAt(boolean active) {
    verify(drawn(active, false, false))
        .drawText(
            any(TextRenderer.class),
            any(OrderedText.class),
            eq(X + (WIDTH - LABEL_WIDTH + 1) / 2),
            eq(Y + (HEIGHT - 8) / 2),
            eq(
                VanillaPalette.opaque(
                    active ? VanillaPalette.BUTTON_TEXT : VanillaPalette.BUTTON_TEXT_DISABLED)),
            eq(true));
  }

  @Test
  void aLabelKeepsAMarginOnEachSide() {
    assertEquals(WIDTH - 2 * ButtonFace.TEXT_MARGIN, ButtonFace.labelRoom(WIDTH));
  }
}
