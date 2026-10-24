package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.mixin.client.HandledScreenAccessor;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public record ScreenPanel(int x, int y, int width, int height) {
  public static ScreenPanel of(HandledScreen<?> screen) {
    final HandledScreenAccessor accessor = (HandledScreenAccessor) screen;
    return new ScreenPanel(
        accessor.totalStorage$panelX(),
        accessor.totalStorage$panelY(),
        accessor.totalStorage$panelWidth(),
        accessor.totalStorage$panelHeight());
  }
}
