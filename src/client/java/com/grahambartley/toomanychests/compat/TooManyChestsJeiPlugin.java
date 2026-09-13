package com.grahambartley.toomanychests.compat;

import com.grahambartley.toomanychests.TooManyChests;
import com.grahambartley.toomanychests.screen.ModScreenButtonAreas;
import java.util.ArrayList;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.Identifier;

@JeiPlugin
public final class TooManyChestsJeiPlugin implements IModPlugin {
  private static final Identifier ID = Identifier.of(TooManyChests.MOD_ID, "jei");

  @Override
  public Identifier getPluginUid() {
    return ID;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGuiContainerHandler(
        (Class<HandledScreen<ScreenHandler>>) (Class<?>) HandledScreen.class, new ModButtonAreas());
  }

  private static final class ModButtonAreas
      implements IGuiContainerHandler<HandledScreen<ScreenHandler>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(HandledScreen<ScreenHandler> screen) {
      final List<Rect2i> areas = new ArrayList<>();
      for (final ClickableWidget button : ModScreenButtonAreas.of(screen)) {
        areas.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
      }
      return areas;
    }
  }
}
