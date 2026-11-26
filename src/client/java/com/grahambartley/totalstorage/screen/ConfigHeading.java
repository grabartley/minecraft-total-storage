package com.grahambartley.totalstorage.screen;

import java.util.List;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

public final class ConfigHeading extends ConfigEntry {
  private final Text heading;

  public ConfigHeading(TextRenderer textRenderer, Text heading) {
    super(textRenderer);
    this.heading = heading;
  }

  @Override
  public void render(
      DrawContext context,
      int index,
      int y,
      int x,
      int entryWidth,
      int entryHeight,
      int mouseX,
      int mouseY,
      boolean hovered,
      float delta) {
    drawHeading(context, heading, x + entryWidth / 2, textTop(y, entryHeight));
  }

  static int textTop(int entryTop, int entryHeight) {
    return entryTop + entryHeight - LINE_HEIGHT;
  }

  @Override
  public List<? extends Element> children() {
    return List.of();
  }

  @Override
  public List<? extends Selectable> selectableChildren() {
    return List.of();
  }
}
