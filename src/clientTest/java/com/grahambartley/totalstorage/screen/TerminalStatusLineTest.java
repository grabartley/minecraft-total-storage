package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.operation.TerminalNoticeTone;
import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import java.util.List;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalStatusLineTest {
  private static final Text HEADLINE = Text.literal("Skipped 2 containers");
  private static final Text MESSAGE = Text.literal("Sorted 9 containers, skipped 2");

  private static TextRenderer textRendererMeasuring(int width) {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.getWidth(any(StringVisitable.class))).thenReturn(width);
    when(textRenderer.getTextHandler()).thenReturn(new TextHandler((codePoint, style) -> 1.0f));
    when(textRenderer.wrapLines(any(), anyInt())).thenReturn(List.of(MESSAGE.asOrderedText()));
    return textRenderer;
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"DONE, false", "PARTIAL, false", "FAILED, true"})
  void onlyAFailureIsWrittenInRed(TerminalNoticeTone tone, boolean failure) {
    assertEquals(
        failure ? VanillaPalette.FAILURE : VanillaPalette.LABEL, TerminalStatusLine.colourOf(tone));
  }

  @Test
  void theTextStartsAfterTheWarningAndEndsWithTheStatusArea() {
    assertEquals(
        TerminalScreenLayout.STATUS_X + WarningGlyph.WIDTH + WarningGlyph.GAP,
        TerminalStatusLine.textLeft());
    assertEquals(
        TerminalScreenLayout.STATUS_X + TerminalScreenLayout.STATUS_WIDTH,
        TerminalStatusLine.textLeft() + TerminalStatusLine.textWidth());
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({"PARTIAL", "FAILED"})
  void theWarningAndTheHeadlineShareTheTonesColour(TerminalNoticeTone tone) {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRendererMeasuring(10);
    final int colour = VanillaPalette.opaque(TerminalStatusLine.colourOf(tone));

    TerminalStatusLine.draw(context, textRenderer, tone, HEADLINE);

    verify(context)
        .fill(
            TerminalScreenLayout.STATUS_X,
            TerminalScreenLayout.STATUS_Y,
            TerminalScreenLayout.STATUS_X + WarningGlyph.WIDTH,
            TerminalScreenLayout.STATUS_Y + WarningGlyph.BAR_HEIGHT,
            colour);
    verify(context)
        .drawText(
            eq(textRenderer),
            any(OrderedText.class),
            eq(TerminalStatusLine.textLeft()),
            eq(TerminalScreenLayout.STATUS_Y),
            eq(colour),
            eq(false));
  }

  @Test
  void aHeadlineThatSaysItAllAndFitsNeedsNoTooltip() {
    assertFalse(
        TerminalStatusLine.needsTooltip(
            textRendererMeasuring(TerminalStatusLine.textWidth()), MESSAGE, MESSAGE));
  }

  @Test
  void aHeadlineThatIsCutShortNeedsATooltip() {
    assertTrue(
        TerminalStatusLine.needsTooltip(
            textRendererMeasuring(TerminalStatusLine.textWidth() + 1), MESSAGE, MESSAGE));
  }

  @Test
  void aHeadlineThatSummarisesALongerMessageNeedsATooltip() {
    assertTrue(TerminalStatusLine.needsTooltip(textRendererMeasuring(10), HEADLINE, MESSAGE));
  }

  @Test
  void theTooltipShowsTheWholeMessageAtTheMouse() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRendererMeasuring(10);

    TerminalStatusLine.drawTooltip(context, textRenderer, MESSAGE, 50, 60);

    verify(textRenderer).wrapLines(eq(MESSAGE), anyInt());
    verify(context)
        .drawTooltip(
            eq(textRenderer),
            argThat(components -> components.size() == 1),
            eq(50),
            eq(60),
            eq((TooltipPositioner) HoveredTooltipPositioner.INSTANCE));
  }
}
