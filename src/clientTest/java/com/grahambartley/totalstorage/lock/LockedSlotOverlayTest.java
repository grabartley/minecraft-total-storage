package com.grahambartley.totalstorage.lock;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.stream.Stream;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.slot.Slot;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LockedSlotOverlayTest {
  static Stream<Arguments> screensOffTheTerminal() {
    return LockCuesTest.screens().filter(screen -> !(Boolean) screen.get()[2]);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("screensOffTheTerminal")
  void aSlotOffTheTerminalGetsNoTint(String name, Screen screen, boolean shown) {
    final DrawContext context = mock(DrawContext.class);

    LockedSlotOverlay.draw(context, screen, mock(Slot.class));

    verify(context, never()).fill(anyInt(), anyInt(), anyInt(), anyInt(), anyInt());
  }
}
