package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.grahambartley.totalstorage.screenhandler.FilterColumnLayout;
import com.grahambartley.totalstorage.screenhandler.TagsSectionLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalChromeTest {
  private static final int LEFT = 40;
  private static final int TOP = 70;
  private static final int WIDTH = 120;
  private static final int HEIGHT = 18;

  private static final int OPAQUE_ACCENT = 0xFF000000 | VanillaPalette.ACCENT;

  private static DrawContext rowDrawnWith(boolean active, boolean hovered) {
    final DrawContext context = mock(DrawContext.class);
    TerminalChrome.drawActiveRow(context, active, hovered, LEFT, TOP, WIDTH, HEIGHT);
    return context;
  }

  private static void verifyDrew(DrawContext context, TerminalSprite sprite) {
    verify(context).drawGuiTexture(eq(sprite.id()), eq(LEFT), eq(TOP), eq(WIDTH), eq(HEIGHT));
  }

  @ParameterizedTest(name = "active={0}")
  @CsvSource({"false", "true"})
  void aRowUnderThePointerIsBevelledWhateverElseItIs(boolean active) {
    verifyDrew(rowDrawnWith(active, true), TerminalSprite.RAISED_LIT);
  }

  @Test
  void aRowAwayFromThePointerIsFlat() {
    verifyDrew(rowDrawnWith(false, false), TerminalSprite.FLAT);
  }

  @Test
  void anActiveRowIsSunkenSoItReadsAsSwitchedOnWithoutRelyingOnTheAccent() {
    verifyDrew(rowDrawnWith(true, false), TerminalSprite.PRESSED);
    verify(rowDrawnWith(true, false), never())
        .drawGuiTexture(eq(TerminalSprite.FLAT.id()), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void anActiveRowKeepsItsAccentWhileThePointerIsOverIt() {
    verify(rowDrawnWith(true, true))
        .fill(
            eq(LEFT + TerminalChrome.ACCENT_INSET),
            eq(TOP + TerminalChrome.ACCENT_INSET),
            eq(LEFT + TerminalChrome.ACCENT_INSET + TerminalChrome.ACCENT_WIDTH),
            eq(TOP + HEIGHT - TerminalChrome.ACCENT_INSET),
            eq(OPAQUE_ACCENT));
  }

  @ParameterizedTest(name = "hovered={0}")
  @CsvSource({"false", "true"})
  void aRowThatIsNotActivePaintsNoAccent(boolean hovered) {
    verify(rowDrawnWith(false, hovered), never())
        .fill(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void aRowThatIsNeverActiveDrawsTheSameChromeAsAnInactiveOne() {
    final DrawContext plain = mock(DrawContext.class);
    TerminalChrome.drawRow(plain, true, LEFT, TOP, WIDTH, HEIGHT);

    verifyDrew(plain, TerminalSprite.RAISED_LIT);
    verify(plain, never()).fill(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void theAccentIsABarDownTheLeftEdgeRatherThanAWashOverTheRow() {
    verify(rowDrawnWith(true, false))
        .fill(
            anyInt(),
            anyInt(),
            eq(LEFT + TerminalChrome.ACCENT_INSET + TerminalChrome.ACCENT_WIDTH),
            anyInt(),
            anyInt());
  }

  @Test
  void aRowNeverDrawsChromeTwice() {
    verify(rowDrawnWith(true, true))
        .drawGuiTexture(any(Identifier.class), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @ParameterizedTest(name = "hovered={0}")
  @CsvSource({"false,RAISED", "true,RAISED_LIT"})
  void anAvailableControlStandsProud(boolean hovered, TerminalSprite expected) {
    assertEquals(expected, TerminalChrome.control(hovered));
    assertEquals(expected, TerminalChrome.controlOrUnavailable(true, hovered));
  }

  @ParameterizedTest(name = "hovered={0}")
  @CsvSource({"false", "true"})
  void anUnavailableControlIsFlatWhetherOrNotThePointerIsOverIt(boolean hovered) {
    assertEquals(TerminalSprite.FLAT, TerminalChrome.controlOrUnavailable(false, hovered));
  }

  @Test
  void everyRowLeavesItsTextClearOfTheAccentBar() {
    assertTrue(
        FilterColumnLayout.TEXT_MARGIN >= TerminalChrome.CONTENT_INSET,
        "a filter row writes its name over the accent bar");
    assertTrue(
        TagsSectionLayout.TEXT_MARGIN >= TerminalChrome.CONTENT_INSET,
        "a tag row writes its name over the accent bar");
  }

  @ParameterizedTest(name = "current={0} hovered={1}")
  @CsvSource({
    "true,false,RAISED",
    "true,true,RAISED",
    "false,false,PRESSED",
    "false,true,RAISED_LIT"
  })
  void theCurrentTabStandsProudAndTheRestRecede(
      boolean current, boolean hovered, TerminalSprite expected) {
    assertEquals(expected, TerminalChrome.tab(current, hovered));
  }
}
