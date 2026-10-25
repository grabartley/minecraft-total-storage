package com.grahambartley.totalstorage.screen;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import net.minecraft.client.gui.DrawContext;
import org.junit.jupiter.api.Test;

class WarningGlyphTest {
  private static final int LEFT = 20;
  private static final int TOP = 30;
  private static final int COLOUR = VanillaPalette.ACCENT;

  @Test
  void theGlyphIsABarAboveADotWithAOnePixelGap() {
    final DrawContext context = mock(DrawContext.class);

    WarningGlyph.draw(context, LEFT, TOP, COLOUR);

    verify(context)
        .fill(
            LEFT,
            TOP,
            LEFT + WarningGlyph.WIDTH,
            TOP + WarningGlyph.BAR_HEIGHT,
            VanillaPalette.opaque(COLOUR));
    verify(context)
        .fill(
            LEFT,
            TOP + WarningGlyph.BAR_HEIGHT + 1,
            LEFT + WarningGlyph.WIDTH,
            TOP + WarningGlyph.HEIGHT,
            VanillaPalette.opaque(COLOUR));
    verify(context, times(2)).fill(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
  }
}
