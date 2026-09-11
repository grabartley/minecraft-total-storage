package com.grahambartley.toomanychests.screen;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TerminalRowChromeTest {
  private static final int LEFT = 40;
  private static final int TOP = 70;
  private static final int WIDTH = 120;
  private static final int HEIGHT = 18;

  private static final int OPAQUE_ACCENT = 0xFF000000 | VanillaPalette.ACCENT;

  private static DrawContext drawnWith(boolean active, boolean hovered) {
    final DrawContext context = mock(DrawContext.class);
    TerminalRowChrome.draw(context, active, hovered, LEFT, TOP, WIDTH, HEIGHT);
    return context;
  }

  private static Identifier spriteOf(TerminalSprite sprite) {
    return Identifier.of("too-many-chests", sprite.name().toLowerCase(java.util.Locale.ROOT));
  }

  @ParameterizedTest(name = "active={0} hovered={1}")
  @CsvSource({"false,false", "false,true", "true,false", "true,true"})
  void everyRowDrawsItsChromeAcrossTheWholeRow(boolean active, boolean hovered) {
    verify(drawnWith(active, hovered))
        .drawGuiTexture(
            org.mockito.ArgumentMatchers.any(Identifier.class),
            eq(LEFT),
            eq(TOP),
            eq(WIDTH),
            eq(HEIGHT));
  }

  @ParameterizedTest(name = "active={0}")
  @CsvSource({"false", "true"})
  void aRowUnderThePointerIsBevelledWhateverElseItIs(boolean active) {
    verify(drawnWith(active, true), never())
        .drawGuiTexture(eq(spriteOf(TerminalSprite.FLAT)), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void anActiveRowKeepsItsAccentWhileThePointerIsOverIt() {
    verify(drawnWith(true, true))
        .fill(
            eq(LEFT + TerminalRowChrome.ACCENT_INSET),
            eq(TOP + TerminalRowChrome.ACCENT_INSET),
            eq(LEFT + TerminalRowChrome.ACCENT_INSET + TerminalRowChrome.ACCENT_WIDTH),
            eq(TOP + HEIGHT - TerminalRowChrome.ACCENT_INSET),
            eq(OPAQUE_ACCENT));
  }

  @ParameterizedTest(name = "hovered={0}")
  @CsvSource({"false", "true"})
  void aRowThatIsNotActivePaintsNoAccent(boolean hovered) {
    verify(drawnWith(false, hovered), never())
        .fill(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
  }

  @Test
  void theAccentIsABarDownTheLeftEdgeRatherThanAWashOverTheRow() {
    verify(drawnWith(true, false))
        .fill(
            anyInt(),
            anyInt(),
            eq(LEFT + TerminalRowChrome.ACCENT_INSET + TerminalRowChrome.ACCENT_WIDTH),
            anyInt(),
            anyInt());
  }
}
