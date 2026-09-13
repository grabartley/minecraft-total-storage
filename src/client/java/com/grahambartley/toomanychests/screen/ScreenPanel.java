package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.mixin.client.HandledScreenAccessor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public record ScreenPanel(int x, int y, int width, int height) {
  public static ScreenPanel of(HandledScreen<?> screen) {
    final HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
    return new ScreenPanel(
        accessor.tooManyChests$panelX(),
        accessor.tooManyChests$panelY(),
        accessor.tooManyChests$panelWidth(),
        accessor.tooManyChests$panelHeight());
  }
}
