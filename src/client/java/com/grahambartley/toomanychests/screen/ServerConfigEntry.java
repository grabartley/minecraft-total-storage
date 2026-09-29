package com.grahambartley.toomanychests.screen;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

public abstract class ServerConfigEntry extends ElementListWidget.Entry<ServerConfigEntry> {
  static final int LABEL_TOP = 3;
  static final int LINE_HEIGHT = 9;
  protected static final int LABEL_COLOUR = 0xFFFFFFFF;

  private final TextRenderer textRenderer;

  protected ServerConfigEntry(TextRenderer textRenderer) {
    this.textRenderer = textRenderer;
  }

  protected final void drawLabel(
      DrawContext context, Text label, int x, int y, int width, int colour) {
    context.drawText(
        textRenderer, TrimmedText.toWidth(textRenderer, label, width), x, y, colour, true);
  }

  protected final void drawHeading(DrawContext context, Text heading, int centreX, int y) {
    context.drawCenteredTextWithShadow(textRenderer, heading, centreX, y, LABEL_COLOUR);
  }
}
