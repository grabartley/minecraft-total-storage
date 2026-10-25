package com.grahambartley.totalstorage.screen;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public final class HintTooltip {
  private HintTooltip() {}

  public static void register() {
    TooltipComponentCallback.EVENT.register(
        data ->
            data instanceof HintTooltipData hints ? new HintTooltipComponent(hints.lines()) : null);
  }

  public static void draw(
      DrawContext context,
      TextRenderer textRenderer,
      List<Text> lines,
      List<HintLine> hints,
      int mouseX,
      int mouseY) {
    final List<TooltipComponent> components = new ArrayList<>();
    for (final OrderedText line :
        WrappedTooltip.wrap(
            lines,
            WrappedTooltip.widthFor(context.getScaledWindowWidth()),
            textRenderer::wrapLines)) {
      components.add(TooltipComponent.of(line));
    }
    if (!hints.isEmpty()) {
      components.add(new HintTooltipComponent(hints));
    }
    if (components.isEmpty()) {
      return;
    }
    context.drawTooltip(
        textRenderer, components, mouseX, mouseY, HoveredTooltipPositioner.INSTANCE);
  }
}
