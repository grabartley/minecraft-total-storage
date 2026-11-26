package com.grahambartley.totalstorage.lock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.grahambartley.totalstorage.screen.TerminalSprite;
import java.util.stream.Stream;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.slot.Slot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

  static Stream<Arguments> screensOffTheTerminal() {
    return LockCuesTest.screens().filter(screen -> !(Boolean) screen.get()[2]);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("screensOffTheTerminal")
  void aSlotOffTheTerminalDrawsNoBadge(String name, Screen screen, boolean shown) {
    final DrawContext context = mock(DrawContext.class);

    LockedSlotBadge.draw(context, screen, mock(Slot.class));

    verify(context, never()).drawGuiTexture(any(), anyInt(), anyInt(), anyInt(), anyInt());
  }
}
