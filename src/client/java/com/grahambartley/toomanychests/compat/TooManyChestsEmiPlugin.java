package com.grahambartley.toomanychests.compat;

import com.grahambartley.toomanychests.screen.ModScreenButtonAreas;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.widget.Bounds;
import java.util.function.Consumer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

public final class TooManyChestsEmiPlugin implements EmiPlugin {
  @Override
  public void register(EmiRegistry registry) {
    registry.addGenericExclusionArea(TooManyChestsEmiPlugin::keepClearOfModButtons);
  }

  private static void keepClearOfModButtons(Screen screen, Consumer<Bounds> to) {
    for (final ClickableWidget button : ModScreenButtonAreas.of(screen)) {
      to.accept(new Bounds(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
    }
  }
}
