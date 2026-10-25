package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HintTooltipComponentTest {
  private static final int GLYPH_WIDTH = 6;
  private static final int LEFT = 12;
  private static final int TOP = 40;

  private static final Text WORDS = Text.literal("drop all");

  private static TextRenderer textRenderer() {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.getWidth(any(StringVisitable.class)))
        .thenAnswer(
            invocation ->
                invocation.<StringVisitable>getArgument(0).getString().length() * GLYPH_WIDTH);
    return textRenderer;
  }

  private static List<HintLine> lines(int count) {
    return Collections.nCopies(count, HintLine.of(HintLine.words(WORDS)));
  }

  @Test
  void noHintsTakeNoRoom() {
    final HintTooltipComponent component = new HintTooltipComponent(List.of());

    assertEquals(0, component.getHeight());
    assertEquals(0, component.getWidth(textRenderer()));
  }

  @ParameterizedTest(name = "{0} lines")
  @ValueSource(ints = {1, 2, 4})
  void hintsSitBelowAGapAboveTheirKeyCaps(int count) {
    assertEquals(
        HintTooltipComponent.TOP_GAP + KeyCaps.height(count) + 2,
        new HintTooltipComponent(lines(count)).getHeight());
  }

  @Test
  void theTooltipIsAsWideAsTheWidestHint() {
    final List<HintLine> hints =
        List.of(
            HintLine.of(HintLine.words(Text.literal("a"))),
            HintLine.of(HintLine.key("shift"), HintLine.words(WORDS)));

    assertEquals(
        KeyCaps.width(textRenderer(), hints),
        new HintTooltipComponent(hints).getWidth(textRenderer()));
  }

  @Test
  void hintWordsAreWrittenInVanillaTooltipGreyWithAShadowBelowTheGap() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();

    new HintTooltipComponent(lines(1)).drawItems(textRenderer, LEFT, TOP, context);

    verify(context)
        .drawText(
            textRenderer,
            WORDS,
            LEFT,
            TOP + HintTooltipComponent.TOP_GAP + KeyCaps.TEXT_INSET,
            VanillaPalette.opaque(Formatting.GRAY.getColorValue()),
            true);
  }

  @Test
  void noHintsDrawNothing() {
    final DrawContext context = mock(DrawContext.class);

    new HintTooltipComponent(List.of()).drawItems(textRenderer(), LEFT, TOP, context);

    verify(context, never())
        .drawText(any(), any(Text.class), anyInt(), anyInt(), anyInt(), anyBoolean());
  }

  @Test
  void aComponentWithoutLinesIsRefused() {
    assertThrows(NullPointerException.class, () -> new HintTooltipComponent(null));
  }
}
