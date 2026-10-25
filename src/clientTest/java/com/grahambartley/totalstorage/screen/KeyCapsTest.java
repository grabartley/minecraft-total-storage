package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.lock.LockedSlotBadge;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class KeyCapsTest {
  private static final int GLYPH_WIDTH = 6;
  private static final int LEFT = 30;
  private static final int TOP = 50;
  private static final int WORDS_COLOUR = 0x123456;

  private static final HintLine.Part SHIFT = new HintLine.Key(Text.literal("Shift"));
  private static final HintLine.Part CLICK = new HintLine.Key(Text.literal("Click"));
  private static final HintLine.Part DROP = new HintLine.Words(Text.literal("drop"));
  private static final HintLine.Part BADGE = new HintLine.LockBadge();

  private static TextRenderer textRenderer() {
    final TextRenderer textRenderer = mock(TextRenderer.class);
    when(textRenderer.getWidth(any(StringVisitable.class)))
        .thenAnswer(
            invocation ->
                invocation.<StringVisitable>getArgument(0).getString().length() * GLYPH_WIDTH);
    return textRenderer;
  }

  private static int keyWidthOf(String label) {
    return KeyCaps.keyWidth(label.length() * GLYPH_WIDTH);
  }

  @ParameterizedTest(name = "label {0} wide")
  @CsvSource({"0, 8", "6, 14", "30, 38"})
  void aKeyCapWrapsItsLabelInPaddingAndABorder(int labelWidth, int expected) {
    assertEquals(expected, KeyCaps.keyWidth(labelWidth));
  }

  static Stream<Arguments> neighbouringParts() {
    return Stream.of(
        Arguments.of("nothing before the first part", null, SHIFT, 0),
        Arguments.of("words before a key", DROP, SHIFT, KeyCaps.GROUP_GAP),
        Arguments.of("a key before words", SHIFT, DROP, KeyCaps.GAP),
        Arguments.of("a key before a key", SHIFT, CLICK, KeyCaps.GAP),
        Arguments.of("words before words", DROP, DROP, KeyCaps.GAP),
        Arguments.of("a badge before a key", BADGE, SHIFT, KeyCaps.GAP));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("neighbouringParts")
  void aNewGroupStartsWiderApartThanThePartsInsideIt(
      String what, HintLine.Part previous, HintLine.Part next, int expected) {
    assertEquals(expected, KeyCaps.gapBefore(previous, next));
  }

  static Stream<Arguments> partWidths() {
    return Stream.of(
        Arguments.of("a key", SHIFT, keyWidthOf("Shift")),
        Arguments.of("words", DROP, "drop".length() * GLYPH_WIDTH),
        Arguments.of("a lock badge", BADGE, LockedSlotBadge.WIDTH));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("partWidths")
  void eachPartTakesTheRoomItDraws(String what, HintLine.Part part, int expected) {
    assertEquals(expected, KeyCaps.partWidth(textRenderer(), part));
  }

  @Test
  void aLineIsAsWideAsItsPartsAndTheGapsBetweenThem() {
    final HintLine line = HintLine.of(SHIFT, DROP, CLICK);

    assertEquals(
        keyWidthOf("Shift")
            + KeyCaps.GAP
            + "drop".length() * GLYPH_WIDTH
            + KeyCaps.GROUP_GAP
            + keyWidthOf("Click"),
        KeyCaps.width(textRenderer(), line));
  }

  @Test
  void severalLinesAreAsWideAsTheWidestOfThem() {
    final HintLine narrow = HintLine.of(SHIFT);
    final HintLine wide = HintLine.of(SHIFT, DROP);

    assertEquals(
        KeyCaps.width(textRenderer(), wide), KeyCaps.width(textRenderer(), List.of(narrow, wide)));
  }

  @Test
  void noLinesTakeNoWidth() {
    assertEquals(0, KeyCaps.width(textRenderer(), List.of()));
  }

  @ParameterizedTest(name = "{0} lines")
  @CsvSource({"-1, 0", "0, 0", "1, 12", "3, 42"})
  void linesStackWithAGapBetweenEachPair(int lines, int expected) {
    assertEquals(expected, KeyCaps.height(lines));
  }

  @Test
  void aKeyIsDrawnAsABevelledCapWithItsLabelInside() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();
    final int width = keyWidthOf("Shift");

    KeyCaps.draw(context, textRenderer, HintLine.of(SHIFT), LEFT, TOP, WORDS_COLOUR, true);

    verify(context)
        .fill(
            LEFT,
            TOP,
            LEFT + width,
            TOP + KeyCaps.HEIGHT,
            VanillaPalette.opaque(VanillaPalette.BEVEL_SHADOW));
    verify(context)
        .fill(
            LEFT + 1,
            TOP + 1,
            LEFT + width - 1,
            TOP + KeyCaps.HEIGHT - 1,
            VanillaPalette.opaque(VanillaPalette.PANEL));
    verify(context)
        .drawText(
            textRenderer,
            ((HintLine.Key) SHIFT).label(),
            LEFT + 1 + KeyCaps.PADDING,
            TOP + KeyCaps.TEXT_INSET,
            VanillaPalette.opaque(VanillaPalette.LABEL),
            false);
  }

  @ParameterizedTest(name = "shadowed={0}")
  @CsvSource({"true", "false"})
  void wordsAreWrittenAfterTheKeyInTheColourAndShadowAskedFor(boolean shadowed) {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();

    KeyCaps.draw(
        context, textRenderer, HintLine.of(SHIFT, DROP), LEFT, TOP, WORDS_COLOUR, shadowed);

    verify(context)
        .drawText(
            textRenderer,
            ((HintLine.Words) DROP).text(),
            LEFT + keyWidthOf("Shift") + KeyCaps.GAP,
            TOP + KeyCaps.TEXT_INSET,
            VanillaPalette.opaque(WORDS_COLOUR),
            shadowed);
  }

  @Test
  void aLockBadgeIsCentredUpAndDownInTheLine() {
    final DrawContext context = mock(DrawContext.class);

    KeyCaps.draw(context, textRenderer(), HintLine.of(BADGE), LEFT, TOP, WORDS_COLOUR, false);

    verify(context)
        .drawGuiTexture(
            TerminalSprite.LOCKED_SLOT.id(),
            LEFT,
            TOP + (KeyCaps.HEIGHT - LockedSlotBadge.HEIGHT) / 2,
            LockedSlotBadge.WIDTH,
            LockedSlotBadge.HEIGHT);
    verify(context, never())
        .drawText(any(), any(Text.class), anyInt(), anyInt(), anyInt(), anyBoolean());
  }

  @Test
  void eachLineIsDrawnBelowTheOneBeforeIt() {
    final DrawContext context = mock(DrawContext.class);
    final TextRenderer textRenderer = textRenderer();
    final Text first = Text.literal("first");
    final Text second = Text.literal("second");

    KeyCaps.drawLines(
        context,
        textRenderer,
        List.of(HintLine.of(HintLine.words(first)), HintLine.of(HintLine.words(second))),
        LEFT,
        TOP,
        WORDS_COLOUR,
        false);

    verify(context)
        .drawText(
            eq(textRenderer),
            eq(first),
            eq(LEFT),
            eq(TOP + KeyCaps.TEXT_INSET),
            anyInt(),
            anyBoolean());
    verify(context)
        .drawText(
            eq(textRenderer),
            eq(second),
            eq(LEFT),
            eq(TOP + KeyCaps.HEIGHT + KeyCaps.LINE_GAP + KeyCaps.TEXT_INSET),
            anyInt(),
            anyBoolean());
  }
}
