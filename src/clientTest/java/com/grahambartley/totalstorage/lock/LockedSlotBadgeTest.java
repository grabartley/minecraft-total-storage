package com.grahambartley.totalstorage.lock;

import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.screen.TerminalSprite;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class LockedSlotBadgeTest {
  private static final int LEFT = 44;
  private static final int TOP = 17;

  @Test
  void aFlatBadgeIsTheFiveBySixPadlockAtTheGivenCorner() {
    final DrawContext context = mock(DrawContext.class);

    LockedSlotBadge.drawFlat(context, LEFT, TOP);

    verify(context).drawGuiTexture(TerminalSprite.LOCKED_SLOT.id(), LEFT, TOP, 5, 6);
  }

  @Test
  void aFlatBadgeLeavesTheMatricesAlone() {
    final DrawContext context = mock(DrawContext.class);

    LockedSlotBadge.drawFlat(context, LEFT, TOP);

    verify(context, never()).getMatrices();
  }

  @Test
  void aBadgeOnASlotIsLiftedAboveTheItemAndThenPutBack() {
    final DrawContext context = mock(DrawContext.class);
    final MatrixStack matrices = mock(MatrixStack.class);
    when(context.getMatrices()).thenReturn(matrices);

    LockedSlotBadge.drawAt(context, LEFT, TOP);

    final InOrder order = inOrder(matrices, context);
    order.verify(matrices).push();
    order.verify(matrices).translate(0.0f, 0.0f, 300.0f);
    order
        .verify(context)
        .drawGuiTexture(
            TerminalSprite.LOCKED_SLOT.id(),
            LEFT,
            TOP,
            LockedSlotBadge.WIDTH,
            LockedSlotBadge.HEIGHT);
    order.verify(matrices).pop();
    verify(matrices).translate(anyFloat(), anyFloat(), anyFloat());
  }
}
