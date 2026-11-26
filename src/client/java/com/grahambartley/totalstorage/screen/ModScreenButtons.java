package com.grahambartley.totalstorage.screen;

import com.grahambartley.totalstorage.config.ClientPreferencesStore;
import com.grahambartley.totalstorage.config.ScreenButtonAnchor;
import com.grahambartley.totalstorage.lock.LockedSlotsAttachment;
import com.grahambartley.totalstorage.sort.ScreenSortPolicy;
import com.grahambartley.totalstorage.sort.SortableSlots;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

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
        ScreenButtons.of(screen instanceof InventoryScreen, isSortable(screen.getScreenHandler()));
    if (carried.isEmpty()) {
      return;
    }
    final ScreenButtonLayout layout =
        new ScreenButtonLayout(anchorFor(screen, carried.size()), carried.size());
    final List<ScreenIconButton> widgets =
        carried.stream().map(button -> widgetFor(screen, button)).toList();
    place(screen, layout, carried, widgets);
    Screens.getButtons(screen).addAll(widgets);
    ScreenEvents.beforeRender(screen)
        .register(
            (rendered, context, mouseX, mouseY, delta) -> place(screen, layout, carried, widgets));
  }

  private static boolean isSortable(ScreenHandler handler) {
    final PlayerEntity player = MinecraftClient.getInstance().player;
    return player != null
        && SortRequests.mayBeOffered()
        && ScreenSortPolicy.allowsHandler(handler)
        && SortableSlots.resolve(handler, player, LockedSlotsAttachment.get(player)).isPresent();
  }

  private static ScreenButtonAnchor anchorFor(HandledScreen<?> screen, int carried) {
    final ScreenButtonAnchor chosen = ClientPreferencesStore.current().screenButtonAnchor();
    if (chosen != ScreenButtonAnchor.INSIDE_TOP_RIGHT) {
      return chosen;
    }
    if (carried > 1) {
      return ScreenButtonAnchor.OUTSIDE_TOP_RIGHT;
    }
    return ScreenButtonBand.isClear(ScreenPanel.of(screen), slotCorners(screen))
        ? chosen
        : ScreenButtonAnchor.OUTSIDE_TOP_RIGHT;
  }

  private static List<ScreenButtonSpot> slotCorners(HandledScreen<?> screen) {
    final ScreenPanel panel = ScreenPanel.of(screen);
    return screen.getScreenHandler().slots.stream().map(slot -> cornerOf(panel, slot)).toList();
  }

  private static ScreenButtonSpot cornerOf(ScreenPanel panel, Slot slot) {
    return new ScreenButtonSpot(panel.x() + slot.x - 1, panel.y() + slot.y - 1);
  }

  private static ScreenIconButton widgetFor(HandledScreen<?> screen, ScreenButton button) {
    return new ScreenIconButton(
        button.sprites(), button.label(), () -> button.press(screen.getScreenHandler().syncId));
  }

  private static void place(
      HandledScreen<?> screen,
      ScreenButtonLayout layout,
      List<ScreenButton> carried,
      List<ScreenIconButton> widgets) {
    final List<ScreenButtonSpot> spots =
        layout.spots(
            ScreenPanel.of(screen), screen.width, screen.height, effectsDrawnBeside(screen));
    for (int index = 0; index < widgets.size(); index++) {
      final ScreenIconButton widget = widgets.get(index);
      widget.setPosition(spots.get(index).x(), spots.get(index).y());
      widget.visible = carried.get(index).isOffered();
      widget.active = carried.get(index).isAvailable();
    }
  }

  private static int effectsDrawnBeside(HandledScreen<?> screen) {
    final PlayerEntity player = MinecraftClient.getInstance().player;
    return screen instanceof AbstractInventoryScreen<?> && player != null
        ? player.getStatusEffects().size()
        : 0;
  }
}
