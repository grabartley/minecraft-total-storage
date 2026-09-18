package com.grahambartley.toomanychests.screen;

import com.grahambartley.toomanychests.config.ClientPreferencesStore;
import com.grahambartley.toomanychests.config.ScreenButtonAnchor;
import com.grahambartley.toomanychests.lock.LockedSlotsAttachment;
import com.grahambartley.toomanychests.sort.ScreenSortPolicy;
import com.grahambartley.toomanychests.sort.SortableSlots;
import java.util.List;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
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
    final ScreenButtonAnchor anchor = anchorFor(screen, carried.size());
    for (int index = 0; index < carried.size(); index++) {
      add(screen, carried.get(index), anchor, index, carried.size());
    }
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

  private static void add(
      HandledScreen<?> screen,
      ScreenButton button,
      ScreenButtonAnchor anchor,
      int index,
      int count) {
    final ScreenIconButton widget =
        new ScreenIconButton(
            button.sprites(), button.label(), () -> button.press(screen.getScreenHandler().syncId));
    followPanel(screen, button, widget, anchor, index, count);
    Screens.getButtons(screen).add(widget);
    ScreenEvents.beforeRender(screen)
        .register(
            (rendered, context, mouseX, mouseY, delta) ->
                followPanel(screen, button, widget, anchor, index, count));
  }

  private static void followPanel(
      HandledScreen<?> screen,
      ScreenButton button,
      ScreenIconButton widget,
      ScreenButtonAnchor anchor,
      int index,
      int count) {
    final ScreenButtonSpot spot = ScreenButtonRow.at(anchor, ScreenPanel.of(screen), index, count);
    widget.setPosition(spot.x(), spot.y());
    widget.visible = button.isOffered();
    widget.active = button.isAvailable();
  }
}
