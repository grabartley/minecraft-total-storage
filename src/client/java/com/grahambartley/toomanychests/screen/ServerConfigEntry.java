package com.grahambartley.toomanychests.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

public abstract class ServerConfigEntry extends ElementListWidget.Entry<ServerConfigEntry> {
  protected static final int LABEL_TOP = 3;
  protected static final int LABEL_COLOUR = 0xFFFFFFFF;

  private final TextRenderer textRenderer;

  protected ServerConfigEntry(TextRenderer textRenderer) {
    this.textRenderer = textRenderer;
  }

  protected final void drawLabel(DrawContext context, Text label, int x, int y, int colour) {
    context.drawTextWithShadow(textRenderer, label, x, y, colour);
  }
}
