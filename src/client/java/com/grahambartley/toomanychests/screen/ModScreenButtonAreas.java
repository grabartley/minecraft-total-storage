package com.grahambartley.toomanychests.screen;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

public final class ModScreenButtonAreas {
  private ModScreenButtonAreas() {}

  public static List<ClickableWidget> of(Screen screen) {
    if (screen == null) {
      return List.of();
    }
    final List<ClickableWidget> drawn = new ArrayList<>();
    for (final ClickableWidget widget : Screens.getButtons(screen)) {
      if (widget instanceof ScreenIconButton && widget.visible) {
        drawn.add(widget);
      }
    }
    return List.copyOf(drawn);
  }
}
