package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.mixin.client.HandledScreenAccessor;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

public final class InventoryQuickStackButton {
  public static final int PANEL_X = 154;

  public static final int PANEL_Y = 3;

  private InventoryQuickStackButton() {}

  public static void register() {
    ScreenEvents.AFTER_INIT.register(
        (client, screen, width, height) -> {
          if (!(screen instanceof InventoryScreen inventory)) {
            return;
          }
          final QuickStackButton button =
              new QuickStackButton(
                  0, 0, InventoryScreenText.of("quick_stack"), QuickStackRequests::send);
          followPanel(inventory, button);
          Screens.getButtons(screen).add(button);
          ScreenEvents.beforeRender(screen)
              .register(
                  (rendered, context, mouseX, mouseY, delta) -> followPanel(inventory, button));
        });
  }

  private static void followPanel(InventoryScreen inventory, QuickStackButton button) {
    final HandledScreenAccessor panel = (HandledScreenAccessor) inventory;
    button.setPosition(
        panel.tooManyChests$panelX() + PANEL_X, panel.tooManyChests$panelY() + PANEL_Y);
    button.active = QuickStackRequests.isOffered();
  }
}
