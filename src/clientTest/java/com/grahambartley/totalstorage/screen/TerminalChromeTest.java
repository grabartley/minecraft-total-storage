package com.grahambartley.totalstorage.screen;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import net.minecraft.client.gui.DrawContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalChromeTest {
  private static final int LEFT = 40;
  private static final int TOP = 70;
  private static final int TRACK = 88;
  private static final int VISIBLE_ROWS = 4;

  private static TerminalScrollbar scrollbarOver(int entries) {
    final ListingScroll scroll = new ListingScroll(1, VISIBLE_ROWS);
    scroll.fit(entries);
    return new TerminalScrollbar(scroll, TRACK);
  }

  @Test
  void aListThatOverflowsDrawsItsThumbInTheTrack() {
    final DrawContext context = mock(DrawContext.class);
    final TerminalScrollbar scrollbar = scrollbarOver(VISIBLE_ROWS * 3);

    TerminalChrome.drawScrollbar(context, scrollbar, LEFT, TOP, 6, TRACK);

    verify(context).drawGuiTexture(TerminalSprite.SUNKEN.id(), LEFT, TOP, 6, TRACK);
    verify(context)
        .drawGuiTexture(
            TerminalSprite.RAISED.id(),
            LEFT,
            TOP + scrollbar.thumbTop(),
            6,
            scrollbar.thumbHeight());
  }

  @Test
  void aListThatFitsShowsAnEmptyTrackWithNoThumb() {
    final DrawContext context = mock(DrawContext.class);

    TerminalChrome.drawScrollbar(context, scrollbarOver(VISIBLE_ROWS), LEFT, TOP, 6, TRACK);

    verify(context).drawGuiTexture(TerminalSprite.SUNKEN.id(), LEFT, TOP, 6, TRACK);
    verify(context, never())
        .drawGuiTexture(eq(TerminalSprite.RAISED.id()), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @ParameterizedTest(name = "{0} entries")
  @CsvSource({"4, SCROLLER_DISABLED", "12, SCROLLER"})
  void theGridScrollerIsTheCreativeOneGreyedWhenThereIsNothingToScroll(
      int entries, VanillaSprite expected) {
    final DrawContext context = mock(DrawContext.class);

    TerminalChrome.drawScroller(context, scrollbarOver(entries), LEFT, TOP, TRACK);

    verify(context)
        .drawGuiTexture(
            expected.id(),
            LEFT,
            TOP + 1,
            TerminalChrome.SCROLLER_WIDTH,
            TerminalChrome.SCROLLER_HEIGHT);
  }

  @Test
  void aFillIsDrawnOpaqueWhateverAlphaItWasGiven() {
    final DrawContext context = mock(DrawContext.class);

    TerminalChrome.fill(context, LEFT, TOP, 5, 3, 0x00C6C6C6);

    verify(context).fill(LEFT, TOP, LEFT + 5, TOP + 3, 0xFFC6C6C6);
  }

  @ParameterizedTest(name = "{0}x{1}")
  @CsvSource({"0, 3", "5, 0", "-2, 3"})
  void anEmptyFillDrawsNothing(int width, int height) {
    final DrawContext context = mock(DrawContext.class);

    TerminalChrome.fill(context, LEFT, TOP, width, height, VanillaPalette.PANEL);

    verify(context, never()).fill(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void aHoverIsTheVanillaTranslucentWash() {
    final DrawContext context = mock(DrawContext.class);

    TerminalChrome.drawHover(context, LEFT, TOP, 10, 8);

    verify(context).fill(LEFT, TOP, LEFT + 10, TOP + 8, VanillaPalette.HOVER);
  }

  @Test
  void anInsetIsShadedAboveAndLitBelowLikeAVanillaSlot() {
    final DrawContext context = mock(DrawContext.class);

    TerminalChrome.drawInset(context, LEFT, TOP, 20, 10);

    verify(context, times(2))
        .fill(
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            eq(VanillaPalette.opaque(VanillaPalette.BEVEL_SHADOW)));
    verify(context, times(2))
        .fill(
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            eq(VanillaPalette.opaque(VanillaPalette.BEVEL_HIGHLIGHT)));
  }

  @Test
  void aRaisedFrameIsOutlinedAndFilledWithThePanelColour() {
    final DrawContext context = mock(DrawContext.class);

    TerminalChrome.drawRaisedFrame(context, LEFT, TOP, 30, 20);

    verify(context, times(4))
        .fill(
            anyInt(),
            anyInt(),
            anyInt(),
            anyInt(),
            eq(VanillaPalette.opaque(VanillaPalette.OUTLINE)));
    verify(context)
        .fill(LEFT + 2, TOP + 2, LEFT + 28, TOP + 18, VanillaPalette.opaque(VanillaPalette.PANEL));
  }

  @ParameterizedTest(name = "selected={0}")
  @CsvSource({"true, PANEL", "false, SUNKEN_FILL"})
  void aSelectedSectionTabTakesThePanelColourAndTheRestRecede(boolean selected, String face) {
    final DrawContext context = mock(DrawContext.class);
    final int colour = face.equals("PANEL") ? VanillaPalette.PANEL : VanillaPalette.SUNKEN_FILL;

    TerminalChrome.drawSectionTab(context, LEFT, TOP, 30, 14, selected);

    verify(context).fill(LEFT + 2, TOP + 2, LEFT + 28, TOP + 14, VanillaPalette.opaque(colour));
  }

  @Test
  void aSelectedSideTabOpensIntoThePanelInsteadOfClosingAgainstIt() {
    final DrawContext open = mock(DrawContext.class);
    final DrawContext closed = mock(DrawContext.class);
    final int joinX = LEFT + 3;

    TerminalChrome.drawSideTab(open, LEFT, TOP, 25, 24, true, joinX);
    TerminalChrome.drawSideTab(closed, LEFT, TOP, 25, 24, false, joinX);

    verify(open)
        .fill(LEFT, TOP, joinX, TOP + 1, VanillaPalette.opaque(VanillaPalette.BEVEL_SHADOW));
    verify(closed)
        .fill(LEFT, TOP, LEFT + 24, TOP + 1, VanillaPalette.opaque(VanillaPalette.OUTLINE));
  }
}
