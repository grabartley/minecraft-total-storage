package com.grahambartley.totalstorage.screen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.screenhandler.TerminalScreenLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;

class PromptFrameTest {
  private static final PromptFrame FRAME = new PromptFrame(40, 30, 180, 60);

  private static DrawContext contextWith(MatrixStack matrices) {
    final DrawContext context = mock(DrawContext.class);
    when(context.getMatrices()).thenReturn(matrices);
    when(context.getScaledWindowWidth()).thenReturn(320);
    when(context.getScaledWindowHeight()).thenReturn(240);
    return context;
  }

  @ParameterizedTest(name = "{0} tall content")
  @CsvSource({"40", "101"})
  void aCentredFrameSitsInTheMiddleOfTheTerminalWindow(int contentHeight) {
    final PromptFrame frame = PromptFrame.centred(contentHeight);
    final int height = PromptFrame.PADDING_TOP + contentHeight + PromptFrame.PADDING_BOTTOM;

    assertEquals(PromptFrame.WIDTH, frame.width());
    assertEquals(height, frame.height());
    assertEquals((TerminalScreenLayout.WINDOW_WIDTH - PromptFrame.WIDTH) / 2, frame.left());
    assertEquals((TerminalScreenLayout.WINDOW_HEIGHT - height) / 2, frame.top());
  }

  @Test
  void theContentStartsInsideThePadding() {
    assertEquals(40 + PromptFrame.PADDING_SIDE, FRAME.contentLeft());
    assertEquals(30 + PromptFrame.PADDING_TOP, FRAME.contentTop());
    assertEquals(90, FRAME.bottom());
  }

  @Test
  void theContentWidthLeavesPaddingOnBothSides() {
    assertEquals(PromptFrame.WIDTH, PromptFrame.CONTENT_WIDTH + 2 * PromptFrame.PADDING_SIDE);
  }

  @ParameterizedTest(name = "({0}, {1}) inside={2}")
  @CsvSource({
    "40, 30, true",
    "219, 89, true",
    "100, 60, true",
    "39, 60, false",
    "220, 60, false",
    "100, 29, false",
    "100, 90, false"
  })
  void onlyPointsWithinTheFrameAreInside(int x, int y, boolean inside) {
    assertEquals(inside, FRAME.isInside(x, y));
  }

  @Test
  void beginningAPromptRaisesItAndDimsTheWholeWindowBehindIt() {
    final MatrixStack matrices = mock(MatrixStack.class);
    final DrawContext context = contextWith(matrices);

    PromptFrame.begin(context);

    final InOrder order = inOrder(matrices, context);
    order.verify(matrices).push();
    order.verify(matrices).translate(0.0F, 0.0F, PromptFrame.ELEVATION);
    order.verify(context).fillGradient(eq(0), eq(0), eq(320), eq(240), anyInt(), anyInt());
  }

  @Test
  void endingAPromptRestoresTheMatrices() {
    final MatrixStack matrices = mock(MatrixStack.class);

    PromptFrame.end(contextWith(matrices));

    verify(matrices).pop();
  }

  @Test
  void thePanelIsDrawnAtTheFrameOffsetFromTheWindowOrigin() {
    final DrawContext context = mock(DrawContext.class);

    FRAME.drawPanel(context, 100, 50);

    verify(context).drawGuiTexture(TerminalSprite.PANEL.id(), 140, 80, 180, 60);
  }
}
