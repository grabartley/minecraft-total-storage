package com.grahambartley.totalstorage.screen;

import java.util.List;
import java.util.Objects;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.util.Formatting;

public final class HintTooltipComponent implements TooltipComponent {
  public static final int TOP_GAP = 4;

  private final List<HintLine> lines;

  public HintTooltipComponent(List<HintLine> lines) {
    this.lines = List.copyOf(Objects.requireNonNull(lines, "lines"));
  }

  @Override
  public int getHeight() {
    return lines.isEmpty() ? 0 : TOP_GAP + KeyCaps.height(lines.size()) + 2;
  }

  @Override
  public int getWidth(TextRenderer textRenderer) {
    return KeyCaps.width(textRenderer, lines);
  }

  @Override
  public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
    KeyCaps.drawLines(
        context,
        textRenderer,
        lines,
        x,
        y + TOP_GAP,
        Objects.requireNonNull(Formatting.GRAY.getColorValue()),
        true);
  }
}
