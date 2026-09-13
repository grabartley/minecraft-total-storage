package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import com.grahambartley.toomanychests.sort.ScreenSortPolicy;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public final class ModScreenButtons {
  private ModScreenButtons() {}

  public static void register() {
    ScreenEvents.AFTER_INIT.register(
        (client, screen, width, height) -> {
          if (screen instanceof HandledScreen<?> handled) {
            addEveryButtonThisScreenCarries(handled);
          }
        });
  }

  private static void addEveryButtonThisScreenCarries(HandledScreen<?> screen) {
    final List<ScreenButton> carried =
        ScreenButtons.of(
            screen instanceof InventoryScreen,
            ScreenSortPolicy.allowsHandler(screen.getScreenHandler()));
    for (int index = 0; index < carried.size(); index++) {
      add(screen, carried.get(index), index);
    }
  }

  private static void add(HandledScreen<?> screen, ScreenButton button, int index) {
    final ScreenIconButton widget =
        new ScreenIconButton(
            button.sprites(), button.label(), () -> button.press(screen.getScreenHandler().syncId));
    followPanel(screen, button, widget, index);
    Screens.getButtons(screen).add(widget);
    ScreenEvents.beforeRender(screen)
        .register(
            (rendered, context, mouseX, mouseY, delta) ->
                followPanel(screen, button, widget, index));
  }

  private static void followPanel(
      HandledScreen<?> screen, ScreenButton button, ScreenIconButton widget, int index) {
    final ScreenButtonSpot spot =
        ScreenButtonRow.at(
            ClientPreferencesStore.current().screenButtonAnchor(), ScreenPanel.of(screen), index);
    widget.setPosition(spot.x(), spot.y());
    widget.visible = button.isOffered();
    widget.active = button.isAvailable();
  }
}
